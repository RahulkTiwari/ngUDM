"""
Application listening to Sharepoint folder for a datasource whether files have been added
"""
# Third party libs
import logging
import argparse
import glob
import time
# Custom libs
import main as mn
from modules.logger import config_object

logging.basicConfig(level=logging.DEBUG)


def get_arguments():
    parser = argparse.ArgumentParser(
        prog='Short sell file listener',
        description='Application listening to a folder location for available files.')

    parser.add_argument(
        '-c', '--source_code',
        required=True,
        help='Provide a source code for this listener.',
    )

    args = parser.parse_args()

    return args.source_code


def main():
    source_code = get_arguments()

    location = f'{config_object["PROCESSING"]["sharepoint_location"]}/{source_code}'
    while True:
        files = glob.glob(f'{location}/*.csv', recursive=False)
        if files:
            logging.info(f'Found files {files} for exchange {source_code}')
            time.sleep(5)
            for each_file in files:
                logging.info(f'Processing file {each_file}')
                mn.main()


if __name__ == '__main__':
    try:
        main()
    except Exception as e:
        logging.fatal(e, exc_info=True)
