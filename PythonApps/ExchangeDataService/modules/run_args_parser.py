# Third party libs
import argparse
# Custom libs
from modules import run_arg_cache as rac
from modules.config import config_object


def get_codes(source_codes):
    codes_string = ''

    if source_codes.strip() == 'all_codes':
        codes_string = config_object['PROCESSING']['supported_codes']
    elif source_codes != '':
        codes_string = source_codes

    codes = [code.strip() for code in codes_string.split(',')]

    return codes


def read_arguments(arguments):

    # Reading run arguments
    parser = argparse.ArgumentParser(
        prog='Short sell retriever',
        description='Retrieves short sell data on exchanges\'s website and find corresponding Refinitiv DataScope Equites security.')

    parser.add_argument(
        '-c', '--source_codes',
        required=True,
        help='Provide a source code (case insensitive) or a comma separated list of source codes for this run. If argument is "all_codes" then all '
             'exchanges will be processed.',
    )

    parser.add_argument(
        '-o', '--out_format',
        required=False,
        default='json',
        choices=['csv', 'json'],
        help='Provide the file format of the output file. Default is json, semicolon separated. Other possible value is csv.',
    )

    parser.add_argument(
        '-e', '--enrich_data',
        required=False,
        default='y',
        choices=['y', 'n'],
        help='Hidden property used during development to prevent that scraped exchange data is enriched with data stored in sdData.'
    )

    parser.add_argument(
        '-j', '--create_jira',
        required=False,
        default='y',
        choices=['y', 'n'],
        help='Hidden property used during development to prevent that jiras are being created when a trdse record is not found in sdData.'
    )

    args = parser.parse_args(arguments)

    rac.source_codes = get_codes(args.source_codes)
    rac.output_format = args.out_format
    rac.enrich_data = True if args.enrich_data == 'y' else False
    rac.create_jira = True if args.create_jira == 'y' else False
