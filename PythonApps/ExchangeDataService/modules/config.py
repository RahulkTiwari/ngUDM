# Third party libs
import configparser
import os
from pathlib import Path
# Custom libs
from modules import run_arg_cache as rac


def get_all_configurations():
    project_dir = os.path.dirname(os.path.abspath(__file__))
    if not rac.test_mode:
        config_loc = Path(project_dir + '/../configurations.ini')
    else:
        config_loc = Path(project_dir + '/../tests/resources/configurations.ini')
    if not config_loc.is_file():
        raise FileNotFoundError
    config_obj = configparser.ConfigParser()
    config_obj.read(config_loc)

    return config_obj


config_object = get_all_configurations()
