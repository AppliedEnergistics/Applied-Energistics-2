import os
import json
from PIL import Image, ImageChops

# Load JSON data
with open('sprites.json', 'r') as f:
    sprites = json.load(f)

# Open the states.png image
image = Image.open('states.png')

# Create the output directory if it doesn't exist
output_dir = '../src/main/resources/assets/ae2/textures/gui/sprites/icons'
os.makedirs(output_dir, exist_ok=True)

# Compare function to check if two images are the same using numpy arrays

# Compare function to check if two images are the same using pixel-by-pixel comparison
def images_are_equal(img1, img2):
    if img1.size != img2.size:
        return False
    for x in range(img1.width):
        for y in range(img1.height):
            p1 = img1.getpixel((x, y))
            p2 = img2.getpixel((x, y))

            # For alpha = 0 it doesnt matter what the transparency color is
            if p1[3] == 0 and p2[3] == 0:
                continue

            if p1 != p2:
                print(f"Diff @ {x},{y}: {p1}!={p2}")
                return False
    return True


# Extract, compare, and save each sprite
for sprite in sprites:
    # Crop the sprite from the image
    cropped_image = image.crop((sprite['x'], sprite['y'], sprite['x'] + sprite['w'], sprite['y'] + sprite['h']))

    # Prepare the output filename with "@dark" appended
    base_filename, ext = os.path.splitext(sprite['filename'])
    output_filename = f"{base_filename}_darkmode{ext}"
    output_path = os.path.join(output_dir,   output_filename)

    # Path to the corresponding icon image
    icon_path = os.path.join(output_dir, sprite['filename'])

    # Check if the icon file exists and compare
    print(base_filename)
    icon_image = Image.open(icon_path)
    if images_are_equal(cropped_image, icon_image):
        continue

    # Save the cropped image
    cropped_image.save(output_path)
