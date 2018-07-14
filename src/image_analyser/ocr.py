from PIL import Image
import pytesseract
import argparse
import cv2
import config_manager
import re


def get_format(path_to_image):
    try:
        img = Image.open(path_to_image)
        return img.format
    except IOError:
        print 'Not an image file or unreadable.'


ap = argparse.ArgumentParser()
ap.add_argument("-i", "--image", type=str, required=True, help='python script needs file name and extenion')
args = vars(ap.parse_args())

config = config_manager.ConfigManager()

id_format = args['image'].split('.')
unique_ID = id_format[0]
image_format = id_format[1]

image_location = config.image_path() + unique_ID + '/' + unique_ID + '.' + image_format

image_read = cv2.imread(image_location, cv2.IMREAD_COLOR)
gray_image = cv2.cvtColor(image_read, cv2.COLOR_BGR2GRAY)

resized_gray_image = cv2.resize(gray_image, (1000, 1000))

pytesseract.pytesseract.tesseract_cmd = config.tesseract()

message = ''
detected_text = ''
log_text = ''

increm = 1
for tresh_it in range(0, 255, 5):
    blur = cv2.blur(gray_image, (5, 5), 0)
    gray = cv2.threshold(gray_image, tresh_it, 255, cv2.THRESH_BINARY)[1]
    resized_image = cv2.resize(gray, (500, 500))

    try:
        detected_text = str(pytesseract.image_to_string(resized_image))
        log_text += "Detectia " + str(increm) + ":\n"
        log_text += detected_text + "\n\n"
        increm += 1
        if detected_text != '' and bool(re.match('^[\w\d !?_\-.,\':;\n]*$', detected_text)) and detected_text not in message:
            message += detected_text + '\n\n'
    except:
        detected_text = ''

if message == '':
    message = 'Text nedetectat!'

print 'Textul detectat: \n' + message

f = open(config.image_path() + unique_ID + '/' + unique_ID + '.txt', 'w')
log_file = open(config.image_path() + unique_ID + '/' + 'log_text.txt', 'w')

f.write(message)
log_file.write(log_text)

f.close()
log_file.close()
