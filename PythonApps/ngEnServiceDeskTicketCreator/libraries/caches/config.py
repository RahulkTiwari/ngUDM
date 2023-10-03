# Third party libs
import configparser


def get_all_configurations():
    config_obj = configparser.ConfigParser()
    config_obj.read('resources/configurations.ini')

    return config_obj


config_object = get_all_configurations()
