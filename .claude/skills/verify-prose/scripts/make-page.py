import sys
from PIL import Image, ImageDraw, ImageFont

out = sys.argv[1]
W, H = 3000, 4000
paragraphs = [
    "It is a truth universally acknowledged, that a single man in possession of a good fortune, must be in want of a wife. However little known the feelings or views of such a man may be on his first entering a neigh-",
    "bourhood, this truth is so well fixed in the minds of the surrounding families, that he is considered the rightful property of some one or other of their daughters.",
    "\"My dear Mr. Bennet,\" said his lady to him one day, \"have you heard that Netherfield Park is let at last?\"",
    "Mr. Bennet replied that he had not. \"But it is,\" returned she; \"for Mrs. Long has just been here, and she told me all about it.\"",
]
page = Image.new("RGB", (2400, 3400), (246, 241, 228))
draw = ImageDraw.Draw(page)
font = ImageFont.truetype("/System/Library/Fonts/Supplemental/Georgia.ttf", 78)
x0, y, width = 180, 260, 2040
for i, paragraph in enumerate(paragraphs):
    words, line = paragraph.split(" "), ""
    first = i != 1
    indent = 110 if first else 0
    for word in words:
        trial = (line + " " + word).strip()
        if draw.textlength(trial, font=font) > width - indent:
            draw.text((x0 + indent, y), line, font=font, fill=(30, 28, 26))
            y += 118
            line, indent = word, 0
        else:
            line = trial
    draw.text((x0 + indent, y), line, font=font, fill=(30, 28, 26))
    y += 118 if i == 0 else 190
photo = Image.new("RGB", (W, H), (58, 46, 38))
rotated = page.rotate(2.0, expand=True, fillcolor=(58, 46, 38), resample=Image.BICUBIC)
photo.paste(rotated, ((W - rotated.width) // 2, (H - rotated.height) // 2))
landscape = photo.rotate(90, expand=True)
exif = Image.Exif()
exif[0x0112] = 6
landscape.save(out, quality=90, exif=exif)
print(out, landscape.size)
