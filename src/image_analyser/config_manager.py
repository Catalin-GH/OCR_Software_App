import json


class ConfigManager:
    json_relative_path = 'C:/Workspace/OCR_Application/src/cfg/configuration.json'
    json_data = ""

    def __init__(self):
        json_file = open(self.json_relative_path, 'r')
        self.json_data = json.load(json_file)

    def tesseract(self):
        return self.json_data["tools"]["tesseract"]

    def image_path(self):
        return self.json_data["images_directory"]
