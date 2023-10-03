# Third party libs
from bs4 import BeautifulSoup
from requests_html import HTMLSession
from datetime import datetime
# Custom libs
from modules.generic_functions import convert_date
from objects.notice import Notice

exchange = 'borsa_italia_idem'


def get_notice_list():

    # Since the url contains the year we might need to look back to previous year's url in case of start of new year (January)
    this_year = datetime.now().year
    this_month = datetime.now().month

    years = [this_year]

    if this_month == 1:
        years.append(this_year - 1)

    notice_list = []

    for each_year in years:

        session = HTMLSession()

        headers = {
            "User-Agent": "Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.141 Safari/537.36"
        }

        url = f'https://www.borsaitaliana.it/derivati/primopiano/comunicatiidemarchivio{each_year}.en.htm'
        page = session.get(url, headers=headers)

        page_content = page.text

        soup = BeautifulSoup(page_content, 'lxml')
        notice_table = soup.find('table', class_='table-editorial-1')

        table_rows = notice_table.find_all('tr')
        notice_table_entries = []

        for each_row in table_rows:
            if len(each_row.find_all('td')) == 3:
                notice_table_entries.append(each_row)

        notice_list.extend(notice_table_entries[1:])

    return notice_list


def set_values(notice_table_row):
    column_values = notice_table_row.find_all('td')
    notice = Notice(exchange)

    # The subject in the p.text element is different across notifications and may also contain e.g. VIEW PDF. In that case it's spans over multiple
    # lines, and therefore split on linebreak and use index 0
    notice_subject = column_values[2].p.text.split('\n')[0]

    notice.exchangeReferenceId = f'{column_values[0].p.text}|{column_values[1].p.text}|{notice_subject}'
    notice.noticeSourceUniqueId = f'{column_values[0].p.text}|{column_values[1].p.text}|{notice_subject}'
    notice.eventInitialUrl = f'https://www.borsaitaliana.it/{column_values[2].find("a")["href"]}'
    notice.eventSubject = f'{column_values[1].p.text} {notice_subject}'
    notice.eventPublishDate = convert_date(column_values[0].p.text, exchange)
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
