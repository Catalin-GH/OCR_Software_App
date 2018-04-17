import cv2
import sys
import numpy as np

image_name = sys.argv[1]

image_path = "C:/Workspace/Image_location/"

image = cv2.imread(image_path + image_name)
cv2.imwrite(image_path + "original_" + image_name, image)

# gray_image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
#
# ret, binary_image = cv2.threshold(gray_image, 127, 255, cv2.THRESH_BINARY)
# cv2.imwrite(image_path + "binary_" + image_name, binary_image)
#
# kernel = np.ones((3, 3), np.uint8)
# image_dilat = cv2.dilate(binary_image, kernel, 1)
#
# cv2.imwrite(image_path + image_name, image_dilat)
