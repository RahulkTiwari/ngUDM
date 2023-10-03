# Third party libs
from datetime import datetime
from requests_html import HTMLSession
from bs4 import BeautifulSoup
# Custom libs
from modules.generic_functions import convert_date
from modules.url_config import url_configuration
from objects.notice import Notice

exchange = 'jpx_public_comments'


def notice_content_check(table_columns):
    check = True
    try:
        _notice_content_check = str(table_columns[0].contents[0])
        if _notice_content_check != ' /Announcement Date ':
            check = False
    except IndexError:
        check = False

    return check


def set_date(date_string):
    if date_string.startswith('May'):
        return_date = convert_date(date_string.replace('May', 'May.'), exchange)
    else:
        return_date = convert_date(date_string, exchange)

    return return_date


def get_notice_list():
    session = HTMLSession()

    headers = {
        "User-Agent": "Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.141 Safari/537.36"
    }
    current_year_url = url_configuration[exchange]['url']

    # Since the url contains the year we might need to look back to previous year's url in case of start of new year (January)
    this_month = datetime.now().month

    url_list = [current_year_url]

    if this_month == 1:
        url_list.append('https://www.jpx.co.jp/english/rules-participants/public-comment/archives-01.html')

    notice_list = []

    for each_url in url_list:

        page = session.get(each_url, headers=headers)

        page_content = page.text

        soup = BeautifulSoup(page_content, 'lxml')
        notice_table_entries = soup.find_all('tr')

        notice_list.extend(notice_table_entries)

    return notice_list


def set_values(notice_table_row):
    columns = notice_table_row.find_all('td')
    notice = Notice(exchange)

    if not notice_content_check(columns):
        if notice_table_row.find_all('th'):
            notice.raise_warning = False
            return notice
        else:
            return notice

    publish_date = set_date(columns[0].text.strip())
    exchange_reference_id = '/'.join(columns[3].a['href'].split('/')[-2:]).replace('.html', '')

    notice.exchangeReferenceId = exchange_reference_id
    notice.noticeSourceUniqueId = exchange_reference_id
    notice.eventInitialUrl = f'https://www.jpx.co.jp{columns[3].a["href"]}'
    notice.eventSubject = columns[3].a.text
    notice.eventPublishDate = publish_date
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
