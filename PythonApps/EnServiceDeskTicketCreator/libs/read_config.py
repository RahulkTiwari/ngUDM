import configparser


LOG_LEVELS = {
    50: 'CRITICAL',
    40: 'ERROR',
    30: 'WARNING',
    20: 'INFO',
    10: 'DEBUG',
    0: 'NOTSET'
}


def get_config():
    config = configparser.ConfigParser()
    config.read('properties/config.ini')

    return config


configuration = get_config()
