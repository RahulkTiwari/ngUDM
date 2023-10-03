# Third party libs
from pathlib import Path
import json
import re
from datetime import datetime
import pandas as pd
# Custom libs
from modules.config import config_object
from modules import run_arg_cache as rac
from modules.logger import main_logger
from modules.generic_functions import convert_string_to_list, create_download_folder


def get_separator():
    """
    Read the value from the configuration.ini. In case the separator is quotes, remove the quotes. Quotes are required in case the
    separator is a comment char of an .ini file.
    :return: separator char
    :rtype: str
    """

    conf_value = config_object['PROCESSING']['output_file_sep']
    separator = re.sub('["\']', '', conf_value)

    return separator


class FileWriter:
    """
    Class to write the list of securities to a file
    """

    def __init__(self, source):

        self.source = source.source_code
        self.file_location = create_download_folder(config_object['PROCESSING']['output_file_dir'])
        self.separator = get_separator()
        self.format = rac.output_format
        self.file_date = datetime.now().strftime('%Y-%m-%dT%H%M%S')
        self.root_file_name = f'rduEds_{self.source}_{self.file_date}'
        self.field_list = convert_string_to_list(config_object['PROCESSING']['output_fields']) + source.exchange_fields

        try:
            Path(self.file_location).mkdir(parents=True, exist_ok=False)
        except FileExistsError:
            pass

    def write(self, source_code_obj):
        securities_obj_list = source_code_obj.securities

        file_content = [each_sec for each_sec in securities_obj_list.values()]

        if self.format == 'json':
            self.to_json(file_content)
        elif self.format == 'csv':
            self.to_csv(file_content)
        else:
            # This can't be reached since run args only allows json and csv. Included for future enhancements
            pass

    def to_json(self, content_json):
        def filter_json(item):
            fields = [*self.field_list, 'exchange_fields']
            key, value = item
            if key in fields:
                return True
            else:
                return False

        with open(f'{self.file_location}/{self.root_file_name}.json', 'w') as feed_file:
            for each_record in content_json:
                filtered_record = dict(filter(filter_json, each_record.items()))
                feed_file.write(json.dumps(filtered_record) + '\n')
        feed_file.close()
        main_logger.debug(f'Created {self.source} file: {self.root_file_name}.json.')

    def to_csv(self, content_json):
        file_df = pd.DataFrame(columns=self.field_list)
        for each_sec in content_json:
            row_df = pd.json_normalize(each_sec)
            row_df.columns = row_df.columns.str.replace(r'exchange_fields.', '')
            file_df = pd.concat([file_df, row_df[self.field_list]])

        file_df.to_csv(
            f'{self.file_location}/{self.root_file_name}.csv',
            sep=self.separator,
            index=False
        )

        main_logger.debug(f'Created {self.source} file: {self.root_file_name}.csv.')
