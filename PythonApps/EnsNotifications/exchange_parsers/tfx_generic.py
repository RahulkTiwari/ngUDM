# Third party libs
from requests_html import HTMLSession
from bs4 import BeautifulSoup
# Custom libs
from ext_libs.googletrans import Translator
from modules.generic_functions import convert_date
from modules.url_config import url_configuration
from objects.notice import Notice
from objects.url import Url


def get_tfx_notice_list(exchange):
    session = HTMLSession()

    headers = {
        "User-Agent": "Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.141 Safari/537.36"
    }
    url = url_configuration[exchange]['url']

    page = session.get(url, headers=headers)

    page_content = page.text

    soup = BeautifulSoup(page_content, 'lxml')
    notice_table = soup.find('ul', class_='snd_news_list01')
    notice_table_entries = notice_table.find_all('li')

    return notice_table_entries


def set_tfx_values(notice_table_row, exchange):
    notice = Notice(exchange)

    exchange_reference_id = Url(notice_table_row.a['href']).last_element

    translator = Translator()
    try:
        en_subject = translator.translate(notice_table_row.a.p.text, src='ja', dest='en').text
    except (AttributeError, TypeError):
        en_subject = f'Unable to translate subject for reference id {exchange_reference_id}'

    notice.exchangeReferenceId = exchange_reference_id
    notice.noticeSourceUniqueId = exchange_reference_id
    notice.eventInitialUrl = notice_table_row.a['href']
    notice.eventSubject = en_subject
    notice.eventPublishDate = convert_date(notice_table_row.a.time.text, exchange)
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
