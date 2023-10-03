# Third party libs
import re


def get_parts(url):
    file_extension = ('pdf', 'docx', 'doc', 'xlsx', 'html', 'htm')

    # Remove protocol
    if url.startswith('http'):
        url = re.sub('http(s)?://', '', url)
    elements = url.split('/')
    elements[len(elements) - 1] = re.sub('[?@#].*', '', elements[len(elements) - 1])
    extension = elements[len(elements) - 1].split('.')[-1] if elements[len(elements) - 1].endswith(file_extension) else None
    parts = {
        'root': '/'.join(elements[:-1]) if extension else '/'.join(elements),
        'document_full': elements[-1] if extension else None,
        'document_extension': extension if extension else None,
        'document_root': elements[-1].replace(f'.{extension}', '') if extension else None,
        'last_element': elements[-1] if not extension else elements[-2],
        'all_elements': elements if not extension else elements[:-1]
    }

    return parts


class Url(object):
    """
    Creates an object from an url to easily access the parts of an url. Attributes are:
     - root: the url without document and its extension, where the extension is the last element if the url ends on 'pdf', 'docx', 'doc', 'xlsx',
       'html' or 'htm'. For example for https://www.foobar.com/part2/part3/doc.html the root is www.foobar.com/part2/part3
     - protocol: either http or https
     - all_elements: a list of all elements of the root, when splitting the url by a forward slash ('/')
     - last_element: the last element of the root
     - parameters: parameters of the url, i.e. the key value parts of the url when splitting on ? (start of parameters) and & (each subsequent
       parameter). For example, https://www.foobar.com/part2?param1=foo&param2=bar param1 and param2 are the parameter keys and foo and bar the
       values. Type is dictionary
     - document_full: if url (excluding parameters) ends on 'pdf', 'docx', 'doc', 'xlsx', 'html' or 'htm' then this last element. For example for
       https://www.foobar.com/part2/part3/doc.html the document = doc.html
     - document_root: the document_full without the extension
     - path_extension: the extension of the document_full
    """

    def __init__(self, url):
        path_parts = get_parts(url)
        self.root = path_parts['root']
        self.protocol = self.__get_protocol(url)
        self.document_full = path_parts['document_full']
        self.document_root = path_parts['document_root']
        self.path_extension = path_parts['document_extension']
        self.last_element = path_parts['last_element']
        self.all_elements = path_parts['all_elements']
        self.parameters = self.__get_parameters(url)

    def __get_parameters(self, url):
        parameters = {}
        parameters_list = re.split('[?&]', url)
        for each_param in parameters_list[1:]:
            try:
                param, value = each_param.split('=')
                parameters[param] = value
            except ValueError:
                pass

        self.parameters = parameters

        return self.parameters

    def __get_protocol(self, url):
        if url.startswith(('http', 'https')):
            self.protocal = url.split(':')[0]
            return self.protocal
        else:
            return None
