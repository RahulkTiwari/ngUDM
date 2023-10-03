# Third party libs
import configparser


def get_all_configurations():
    config_obj = configparser.ConfigParser(interpolation=None)
    config_obj.read('url_config.ini')

    return config_obj


url_configuration = get_all_configurations()
