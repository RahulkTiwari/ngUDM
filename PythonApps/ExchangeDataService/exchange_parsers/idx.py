# Third party libs
from playwright.sync_api import sync_playwright
import shutil
import zipfile
import pandas as pd
import glob
import os
import time
# Custom libs
from modules.source_config import source_config_obj
from modules.generic_functions import create_download_folder
from modules.config import config_object
from modules.logger import main_logger
from objects.exchange_data import ExchangeData

source_code = 'idx'
root_dir = config_object['PROCESSING']['resource_root_dir']
destination_folder_path = create_download_folder(f'{root_dir}/exchange_files/{source_code}/downloads')


def archive_files():
    files = glob.glob(f'{destination_folder_path}/*.*', recursive=False)
    archive_folder = create_download_folder(f'{destination_folder_path}/archive')
    for each_file in files:
        # Using copy instead of move to overwrite in case the file already exists
        shutil.copy(each_file, archive_folder)
        os.remove(each_file)


def retrieve_data():

    exchange_data = ExchangeData()

    file_name = None
    main_url = source_config_obj[source_code]['url']

    # Downloading the latest zip file from the website
    user_agent = (
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) '
        'AppleWebKit/537.36 (KHTML, like Gecko) '
        'Chrome/69.0.3497.100 Safari/537.36'
    )
    playwright = sync_playwright().start()
    browser = playwright.firefox.launch(headless=True)
    context = browser.new_context(user_agent=user_agent)

    page = context.new_page()
    try:
        page.goto(main_url)
        time.sleep(5)

        with page.expect_download() as download_info:
            page.locator('//*[@id="vgt-table"]/tbody/tr[1]/td[3]/span/span/a/span').click()
        download = download_info.value
        file_name = download.suggested_filename
        download.save_as(os.path.join(destination_folder_path, file_name))

        context.close()
        browser.close()
        playwright.stop()
    except Exception as ex:
        context.close()
        browser.close()
        playwright.stop()
        main_logger.debug(f'Unable to acces idx website due to...\n{ex}\nClosing browser instance')

    # Extracting the zip file
    with zipfile.ZipFile(f'{destination_folder_path}/{file_name}', 'r') as zObject:
        zObject.extractall(path=destination_folder_path)

    # Findign the Excel's filename. Only one so should be first element of list
    short_sell_excel = glob.glob(f'{destination_folder_path}/*.xlsx')[0]

    # 'Try' since there are not always short sell eligible securities. Then we want to return empty dict
    try:
        ss_df = pd.read_excel(
            short_sell_excel,
            sheet_name=[source_config_obj[source_code]['sheet']],
            header=3,
            converters={
                'No.': str
            }
        )[source_config_obj[source_code]['sheet']]

        exchange_data.data = {ticker: {'short_sell': 'Y'} for ticker in ss_df['Kode'].values.tolist()}
        exchange_data.success = True

    except ValueError:
        main_logger.info('Currently no short sell eligible securities detected for Indonesia')

    archive_files()

    return exchange_data
