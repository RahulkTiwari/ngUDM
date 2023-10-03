# Third party libs
from bs4 import BeautifulSoup
from playwright.sync_api import sync_playwright
# Custom libs
from modules.source_config import source_config_obj
from modules.logger import main_logger
from objects.exchange_data import ExchangeData


source_code = 'ads'

ignore_rows = ['', 'Symbol', 'Financials', 'Consumer Staples', 'Real Estate', 'Industrial', 'Energy', 'Telecommunication', 'Basic Materials',
               'Consumer Discretionary', 'Utilities', 'Technology', 'Other', 'Health Care']
user_agent = (
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) '
    'AppleWebKit/537.36 (KHTML, like Gecko) '
    'Chrome/69.0.3497.100 Safari/537.36'
)


def append_check(row):
    check = False
    tds = row.find_all('td')
    try:
        # Check if not is a header and if shortsell is blank and if it's actively trading
        if tds[1].text not in ignore_rows and tds[6].text.strip() == '' and tds[7].text == 'Active':
            check = True
    except IndexError:
        pass

    return check


def get_isins_from_main():

    exchange_data = ExchangeData()

    playwright = sync_playwright().start()

    browser = playwright.chromium.launch(headless=True)
    page = browser.new_page(user_agent=user_agent)

    url = source_config_obj[source_code]['url_main']

    try:
        page.goto(url, timeout=30000)
        page.wait_for_selector('text=eng Market Group')

        page_content = page.content()

        soup = BeautifulSoup(page_content, 'lxml')

        short_sell_table = soup.find('div', string='Symbol').parent.parent.parent
        table_rows = short_sell_table.find_all('tr')

        for each_elem in table_rows:
            if append_check(each_elem):
                exchange_data.data[each_elem.find_all('td')[1].text] = {'short_sell': 'Y'}

        browser.close()
        playwright.stop()
        exchange_data.success = True

    except Exception as e:
        main_logger.info(e, exc_info=True)
        browser.close()
        playwright.stop()

    return exchange_data


def retrieve_data():

    exchange_data = get_isins_from_main()

    return exchange_data
