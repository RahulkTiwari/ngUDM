# Third party libs
import argparse
# Custom libs
from modules.config import config_object


def get_exchanges(source_codes):
    codes_string = ''

    if source_codes.strip() == 'all_codes':
        codes_string = config_object['PROCESSING']['supported_urls']
    elif source_codes != '':
        codes_string = source_codes

    codes = [code.strip() for code in codes_string.split(',')]

    return codes


def get_run_arguments():

    parser = argparse.ArgumentParser(
        prog='ENS notification scraper',
        description='Scans notification for supported exchanges\' websites for newly issued notifications.')

    parser.add_argument(
        '-e', '--exchanges',
        required=True,
        help='Provide an exchange or a comma separated list of exchange for this run. If argument is "all_codes" then all exchanges will '
             'be processed.'
    )

    parser.add_argument(
        '-j', '--create_jiras',
        required=False,
        default='y',
        choices=['y', 'n'],
        help='Indicate if the application needs to create service desk ticket ("y") or not ("n")'
    )

    args = parser.parse_args()

    run_args = {
        'exchanges': get_exchanges(args.exchanges),
        'create_jiras': True if args.create_jiras.strip() == 'y' else False
    }

    return run_args
