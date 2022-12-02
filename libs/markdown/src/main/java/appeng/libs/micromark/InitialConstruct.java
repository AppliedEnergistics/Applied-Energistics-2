package appeng.libs.micromark;

/**
 * Like a construct, but `tokenize` does not accept `ok` or `nok`.
 */
public class InitialConstruct extends Construct {

    public State tokenize(TokenizeContext context, Tokenizer.Effects effects) {
        return this.tokenize.tokenize(context, effects, null, null);
    }

}
