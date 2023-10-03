# Third party libs
import importlib
# Custom libs
from objects.write_file import FileWriter
from modules.logger import main_logger
from modules.source_config import source_config_obj
from objects.source_code import SourceCode
from modules.securities_iterator import iterate_securities


def fetch_data(source_code):
    """
    Main function for each exchange to retrieve a list of securities from the exchange's website, find the corresponding sdData document and
    write the combined data into a feed file.
    In case no corresponding sdData record found then a skeletal record is written to the feedfile. In case multiple sdData records are found then
    based on certain criteria one is picked to enrich the exchange record.
    :param source_code: source code of the exchange
    """

    main_logger.debug(f'Start collecting the data for {source_code}, mic codes {", ".join(source_config_obj[source_code]["mic"].split(","))}')

    # Instantiate source code object and loading source code's module
    source_code_obj = SourceCode(source_code)
    exchange_module = importlib.import_module(f'exchange_parsers.{source_code}')

    # Scraping a list of securities from the exchange's website either returned as a list of ids or as a dict of data with the id as key
    source_code_obj.exchange_data = exchange_module.retrieve_data()

    if source_code_obj.exchange_data.success:
        # Iterate over the list of securities to prepare for writing to the file
        source_code_obj = iterate_securities(source_code_obj)

        file_writer = FileWriter(source_code_obj)
        file_writer.write(source_code_obj)

        main_logger.info(f'Finished processing {source_code}')
        return_code = 0
    else:
        main_logger.warning(f'Finished processing with errors {source_code}')
        return_code = 1

    return return_code
