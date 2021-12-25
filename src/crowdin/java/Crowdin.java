import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.siegmar.fastcsv.reader.NamedCsvReader;
import de.siegmar.fastcsv.writer.CsvWriter;
import de.siegmar.fastcsv.writer.LineDelimiter;
import de.siegmar.fastcsv.writer.QuoteStrategy;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;
import java.util.TreeMap;

/**
 * Manages our translations on https://crwd.in/ae-2
 * - Uploads the en_us.json as the source file for making translations
 * - Downloads translated strings and rebuilds them into language files
 */
public class Crowdin {

    /**
     * Key is the lang-code in Minecrafat, Value is the lang-code in Crowdin.
     */
    private static final Map<String, String> LANG_MAPPING = new HashMap<>();

    static {
        LANG_MAPPING.put("fr_fr", "fr");
        LANG_MAPPING.put("hu_hu", "hu");
        LANG_MAPPING.put("es_es", "es");
        LANG_MAPPING.put("cs_cz", "cs");
        LANG_MAPPING.put("ja_jp", "ja");
        LANG_MAPPING.put("it_it", "it");
        LANG_MAPPING.put("ko_kr", "ko");
        LANG_MAPPING.put("sv_se", "sv");
        LANG_MAPPING.put("pt_br", "pt-BR");
        LANG_MAPPING.put("en_gb", "en-GB");
        LANG_MAPPING.put("ro_ro", "ro");
        LANG_MAPPING.put("ru_ru", "ru");
        LANG_MAPPING.put("de_de", "de");
        LANG_MAPPING.put("zh_cn", "zh-CN");
        LANG_MAPPING.put("zh_tw", "zh-TW");
    }

    private static final String CROWDIN_BASE_URL = "https://appliedenergistics2.crowdin.com/api/v2";

    /**
     * Folder where en_us.json is
     */
    private static final String SOURCE_FOLDER = "src/generated/resources/assets/ae2/lang";

    /**
     * Folder where the translated JSON files are
     */
    private static final String DESTINATION_FOLDER = "src/main/resources/assets/ae2/lang";

    /**
     * We use a fake translations.csv to store our translations on Crowdin.
     */
    private static final String TRANSLATIONS_CSV = "translations.csv";

    // CSV Columns
    private static final String COL_ID = "ID";
    private static final String COL_TEXT = "Text";

    private static final Gson GSON = new Gson().newBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final String projectId = "1";

    private final String baseUrl;

    private final String token;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public Crowdin(String baseUrl, String token) {
        this.baseUrl = baseUrl;
        this.token = token;
    }

    public static void main(String[] args) throws Exception {
        String token = System.getenv("CROWDIN_TOKEN");
        if (token == null) {
            System.err.println("Missing CROWDIN_TOKEN environment variable");
            System.exit(-1);
        }

        String branch = System.getenv("GIT_BRANCH");
        if (branch == null) {
            System.err.println("Missing GIT_BRANCH environment variable");
            System.exit(-1);
        }

        var crowdin = new Crowdin(CROWDIN_BASE_URL, token);
        if (args.length < 1 || args[0].equals("upload_source")) {
            crowdin.uploadSourceFile(branch, Paths.get(SOURCE_FOLDER));
        } else if (args[0].equals("upload_translations")) {
            crowdin.uploadTranslations(branch, Paths.get(DESTINATION_FOLDER));
        } else if (args[0].equals("update_translations")) {
            crowdin.downloadTranslations(branch, Paths.get(DESTINATION_FOLDER));
        }
    }

    private void uploadSourceFile(String branch, Path folder) throws Exception {
        var storageId = createAndUploadCsvFile(createCsvFile(folder, "en_us"), "en_us");

        addCsvSourceFile(branch, storageId);
    }

    /**
     * Uploads all existing translations for a branch. Should be a one-time task.
     */
    private void uploadTranslations(String branch, Path folder) throws Exception {
        // Get the file id of the source translation.csv we'll upload the translations for
        var branchId = getBranchId(branch).orElseThrow(() -> new RuntimeException("Branch " + branch + " does not exist."));
        var fileId = getFileId(branchId, TRANSLATIONS_CSV).orElseThrow(() -> new RuntimeException("translation.csv does not exist in " + branch));

        for (var entry : LANG_MAPPING.entrySet()) {
            uploadTranslations(folder, fileId, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Uploads existing translations for a specific language.
     */
    private void uploadTranslations(Path folder, long fileId, String sourceLang, String destLang) throws Exception {
        var storageId = createAndUploadCsvFile(createCsvFile(folder, sourceLang), sourceLang);

        var payload = new JsonObject();
        payload.addProperty("storageId", storageId);
        payload.addProperty("fileId", fileId);

        var request = newJsonRequest("/projects/" + projectId + "/translations/" + destLang);
        var response = sendJsonRequest(request.POST(jsonBody(payload)).build());
        System.out.println(response.body());
    }

    private void downloadTranslations(String branch, Path folder) throws Exception {
        var branchId = getBranchId(branch).orElseThrow(() -> new RuntimeException("Couldn't find branch " + branch));
        var fileId = getFileId(branchId, TRANSLATIONS_CSV).orElseThrow(() -> new RuntimeException("Couldn't find file " + TRANSLATIONS_CSV + " on branch" + branch));

        for (var langEntry : LANG_MAPPING.entrySet()) {
            downloadTranslations(fileId, langEntry.getKey(), langEntry.getValue(), folder);
        }
    }

    /**
     * Download the translated CSV file and convert it back to a JSON file.
     *
     * @return True if the translations actually changed compared to the previous version.
     */
    private boolean downloadTranslations(long fileId, String minecraftLang, String crowdinLang, Path folder) throws Exception {
        System.out.println("Updating translations for " + minecraftLang);

        var payload = new JsonObject();
        payload.addProperty("targetLanguageId", crowdinLang);
        // Untranslated strings are automatically inherited from en_us.json
        payload.addProperty("skipUntranslatedStrings", true);

        var builder = newJsonRequest("/projects/" + projectId + "/translations/builds/files/" + fileId);

        var etagFile = folder.resolve(minecraftLang + ".etag");
        if (Files.exists(etagFile)) {
            var etag = Files.readString(etagFile).trim();
            builder.header("If-None-Match", etag);
            System.out.println("Using ETAG: " + etag);
        }
        var request = builder.POST(jsonBody(payload)).build();
        var response = sendJsonRequest(request, true);
        if (response == null) {
            System.out.println("Translations for " + minecraftLang + " are unmodified!");
            return false;
        }

        var data = response.body().getAsJsonObject("data");
        var url = data.getAsJsonPrimitive("url").getAsString();
        var etag = data.getAsJsonPrimitive("etag").getAsString();

        // Get the actual file content
        System.out.println("Got ETAG: " + etag);
        System.out.println("Downloading " + url);
        var translationCsvContent = httpClient.send(
                HttpRequest.newBuilder(URI.create(url)).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        if (translationCsvContent.statusCode() != 200) {
            throw new RuntimeException("Failed to download translated file content: " + translationCsvContent.statusCode()
                    + " " + translationCsvContent.body());
        }

        var translationMap = new TreeMap<>();
        try (var csv = NamedCsvReader.builder().quoteCharacter('"').fieldSeparator(',').build(translationCsvContent.body())) {
            csv.forEach(row -> {
                // Skip empty translations
                var text = row.getField(COL_TEXT);
                if (!text.isEmpty()) {
                    translationMap.put(row.getField(COL_ID), text);
                }
            });
        }

        var jsonFile = folder.resolve(minecraftLang + ".json");
        Files.writeString(jsonFile, GSON.toJson(translationMap), StandardCharsets.UTF_8);
        Files.writeString(etagFile, etag);

        return true;
    }

    private long createAndUploadCsvFile(String csvString, String lang) throws Exception {
        var uploadCsvFile = newRequest("/storages")
                .header("Crowdin-API-FileName", lang + ".csv")
                .POST(HttpRequest.BodyPublishers.ofString(csvString))
                .build();
        var response = sendJsonRequest(uploadCsvFile);

        return response.body().getAsJsonObject("data").getAsJsonPrimitive("id").getAsLong();
    }

    private String createCsvFile(Path folder, String lang) throws IOException {
        // Expects the en_us.json File to be present
        var langPath = folder.resolve(lang + ".json");
        JsonObject root;
        try (var reader = Files.newBufferedReader(langPath, StandardCharsets.UTF_8)) {
            root = GSON.fromJson(reader, JsonObject.class);
        }

        // Convert to CSV
        var csvStringWriter = new StringWriter();

        try (var csvWriter = CsvWriter.builder()
                .fieldSeparator(',')
                .quoteCharacter('"')
                .quoteStrategy(QuoteStrategy.REQUIRED)
                .lineDelimiter(LineDelimiter.LF)
                .build(csvStringWriter)) {
            csvWriter.writeRow(COL_ID, COL_TEXT);
            for (var entry : root.entrySet()) {
                csvWriter.writeRow(entry.getKey(), entry.getValue().getAsString());
            }
        }

        return csvStringWriter.toString();
    }

    private void addCsvSourceFile(String branch, long storageId) throws Exception {
        var branchId = createBranch(branch);

        var payload = new JsonObject();
        payload.addProperty("storageId", storageId);
        var importOptions = new JsonObject();
        payload.add("importOptions", importOptions);
        var scheme = new JsonObject();
        importOptions.addProperty("firstLineContainsHeader", true);
        importOptions.add("scheme", scheme);
        scheme.addProperty("identifier", 0);
        scheme.addProperty("sourceOrTranslation", 1);

        var existingFileId = getFileId(branchId, TRANSLATIONS_CSV);
        if (existingFileId.isPresent()) {
            System.out.println("Updating existing file " + existingFileId.getAsLong());
            // Update instead of add
            var request = newJsonRequest("/projects/" + projectId + "/files/" + existingFileId.getAsLong())
                    .PUT(jsonBody(payload))
                    .build();
            sendJsonRequest(request);
        } else {
            payload.addProperty("branchId", branchId);
            payload.addProperty("name", TRANSLATIONS_CSV);
            payload.addProperty("title", "Translations");
            payload.addProperty("type", "csv");

            var request = newJsonRequest("/projects/" + projectId + "/files")
                    .POST(jsonBody(payload))
                    .build();
            sendJsonRequest(request);
        }

        System.out.println("Successfully uploaded base files to Crowdin");
    }

    private long createBranch(String branch) throws Exception {

        var existingBranchId = getBranchId(branch);
        if (existingBranchId.isPresent()) {
            System.out.println("Reusing existing branch " + branch + " with id " + existingBranchId.getAsLong());
            return existingBranchId.getAsLong();
        }

        var payload = new JsonObject();
        payload.addProperty("name", branch);

        var request = newJsonRequest("/projects/" + projectId + "/branches")
                .POST(jsonBody(payload))
                .build();

        var response = sendJsonRequest(request);

        var branchId = response.body().getAsJsonObject("data").getAsJsonPrimitive("id").getAsLong();
        System.out.println("Created branch " + branch + " with id " + branchId);
        return branchId;
    }

    private OptionalLong getBranchId(String branch) throws Exception {
        var request = newJsonRequest("/projects/" + projectId + "/branches?name=" + branch)
                .GET()
                .build();
        var response = sendJsonRequest(request);

        for (var resultEl : response.body().getAsJsonArray("data")) {
            if (resultEl instanceof JsonObject resultObj) {
                resultObj = resultObj.getAsJsonObject("data");
                if (resultObj.getAsJsonPrimitive("name").getAsString().equals(branch)) {
                    var branchId = resultObj.getAsJsonPrimitive("id");
                    return OptionalLong.of(branchId.getAsLong());
                }
            }
        }

        return OptionalLong.empty();
    }

    private OptionalLong getFileId(long branchId, String filename) throws Exception {
        var request = newJsonRequest("/projects/" + projectId + "/files?branchId=" + branchId + "&filter=" + filename)
                .GET()
                .build();
        var response = sendJsonRequest(request);

        for (var resultEl : response.body().getAsJsonArray("data")) {
            if (resultEl instanceof JsonObject resultObj) {
                resultObj = resultObj.getAsJsonObject("data");
                if (resultObj.getAsJsonPrimitive("name").getAsString().equals(filename)) {
                    var fileId = resultObj.getAsJsonPrimitive("id");
                    return OptionalLong.of(fileId.getAsLong());
                }
            }
        }

        return OptionalLong.empty();
    }

    private HttpRequest.BodyPublisher jsonBody(JsonElement payload) {
        return HttpRequest.BodyPublishers.ofString(GSON.toJson(payload));
    }

    private HttpResponse<JsonObject> sendJsonRequest(HttpRequest request) throws Exception {
        return sendJsonRequest(request, false);
    }

    private HttpResponse<JsonObject> sendJsonRequest(HttpRequest request, boolean allowUnmodified) throws Exception {
        System.out.println(request.method() + " " + request.uri());
        var response = httpClient.send(request, jsonHandler());
        if (allowUnmodified && response.statusCode() == 301) {
            return null;
        }
        if (response.statusCode() < 200 || response.statusCode() > 201) {
            System.err.println("Got unexpected HTTP status code " + response.statusCode() + " from Crowdin.");
            System.err.println(response.body());
            System.exit(-1);
        }
        return response;
    }

    private HttpRequest.Builder newJsonRequest(String path) {
        return newRequest(path)
                .header("Content-Type", "application/json");
    }

    private HttpRequest.Builder newRequest(String path) {
        return HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + token)
                .uri(URI.create(baseUrl + path));
    }

    private HttpResponse.BodyHandler<JsonObject> jsonHandler() {
        return responseInfo -> HttpResponse.BodySubscribers.mapping(
                HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8),
                responseBody -> GSON.fromJson(responseBody, JsonObject.class)
        );
    }

}
