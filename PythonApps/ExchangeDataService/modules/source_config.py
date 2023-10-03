# Third party libs
import configparser
import os
from pathlib import Path
# Custom libs
from modules import run_arg_cache as rac


def get_all_configurations():
    project_dir = os.path.dirname(os.path.abspath(__file__))
    if not rac.test_mode:
        config_loc = Path(project_dir + '/../source_config.ini')
    else:
        config_loc = Path(project_dir + '/../tests/resources/source_config.ini')
    if not config_loc.is_file():
        raise FileNotFoundError

    config_obj = configparser.ConfigParser()
    config_obj.read(config_loc)

    return config_obj


source_config_obj = get_all_configurations()
