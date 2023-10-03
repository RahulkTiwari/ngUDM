# Third party libs
import argparse
import pandas as pd
import time
# Custom libs
from modules.securities_iterator import iterate_securities
from modules.logger import main_logger
from objects.write_file import FileWriter
from objects.source_code import SourceCode
from modules.source_config import source_config_obj
from objects.exchange_data import ExchangeData
from modules import run_arg_cache as rac


def check_yes(df_value):
    result = 'Y' if pd.notnull(df_value) and df_value != 'N' else ''
    return result


def main(securities_excel):
    main_logger.info('Start processing input Excel....')

    excel_df = pd.read_excel(
        securities_excel,
        sheet_name='input data',
        header=None,
        dtype=str,
        keep_default_na=False,
        na_values=['-1.#IND', '1.#QNAN', '1.#IND', '-1.#QNAN', '#N/A N/A', '#N/A', 'n/a', '#NA', 'null', 'NaN', '-NaN', 'nan', '-nan', '']
    )

    source_code = excel_df.iloc[0, 1]
    source_config = source_config_obj[source_code]
    src_cd_instance = SourceCode(source_code)

    try:
        additional_fields = source_config['additional_fields']
    except KeyError:
        additional_fields = None

    securities_df = pd.DataFrame(
        data=excel_df.iloc[4:],
        index=None
    ).reset_index(drop=True)
    securities_df.columns = excel_df.iloc[3]

    # Drop rows without exchange id
    securities_df = securities_df[securities_df['exchange ids'].notnull()]

    for idx, each_security in securities_df.iterrows():
        src_cd_instance.exchange_data.data[each_security['exchange ids']] = {
            'short_sell': check_yes(each_security['short_sell'])
        }
        if additional_fields:
            for each_field in additional_fields.split(','):
                each_field = each_field.strip()
                src_cd_instance.exchange_data.data[each_security['exchange ids']][each_field] = each_security[each_field]

    src_cd_instance = iterate_securities(src_cd_instance)

    file_writer = FileWriter(src_cd_instance)
    file_writer.write(src_cd_instance)

    main_logger.info(f'Finished processing manual input for {source_code}')


if __name__ == '__main__':

    try:
        parser = argparse.ArgumentParser()
        parser.add_argument(
            '--manual_input',
            help='Provide the input Excel containing the the list of securities',
            required=True)

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

        args = parser.parse_args()
        rac.enrich_data = True if args.enrich_data == 'y' else False
        rac.create_jira = True if args.create_jira == 'y' else False

        main_logger.info(f'Processing manual data for Excel {args.manual_input}')
        main(args.manual_input)

        time.sleep(5)

    except Exception as e:
        main_logger.fatal(e, exc_info=True)
