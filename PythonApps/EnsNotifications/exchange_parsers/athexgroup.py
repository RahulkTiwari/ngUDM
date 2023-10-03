# Third party libs
from bs4 import BeautifulSoup
from requests_html import HTMLSession
import re
# Custom libs
from modules.generic_functions import convert_date
from modules.url_config import url_configuration
from objects.notice import Notice

exchange = 'athexgroup'


def get_ref_id(url):
    start_index = re.search('/document/id/', url).end()
    end_index = re.search('\?', url).start()

    return str(url[start_index:end_index])


def get_notice_list():
    session = HTMLSession()

    headers = {
        "User-Agent": "Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.141 Safari/537.36"
    }

    url = url_configuration[exchange]['url']
    page_content = session.get(url, headers=headers)
    page_cookies = page_content.cookies.items()
    cookies = {}
    for each_cookie in page_cookies:
        cookies[each_cookie[0]] = each_cookie[1]

    url = 'https://www.athexgroup.gr/web/guest/members-trading-deriv-corp-actions?p_p_id=101_INSTANCE_la40Cn0UJnTr&p_p_lifecycle=0&' \
          'p_p_state=exclusive&p_p_mode=view&p_p_col_id=column-1&p_p_col_pos=2&p_p_col_count=3&' \
          '_101_INSTANCE_la40Cn0UJnTr_noAjaxRendering=https://www.athexgroup.gr/web/guest/members-trading-deriv-corp-actions'
    page_content = session.get(url, cookies=cookies, headers=headers)

    soup = BeautifulSoup(page_content.text, 'html.parser')

    notices_table_entries = soup.find_all('a')[:-1]

    return notices_table_entries


def set_values(notice_table_row):
    notice = Notice(exchange)

    notice.exchangeReferenceId = get_ref_id(notice_table_row['href'])
    notice.noticeSourceUniqueId = get_ref_id(notice_table_row['href'])
    notice.eventInitialUrl = notice_table_row['href']
    notice.eventSubject = notice_table_row.text
    notice.eventPublishDate = convert_date(notice_table_row.text[:7], exchange)
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
