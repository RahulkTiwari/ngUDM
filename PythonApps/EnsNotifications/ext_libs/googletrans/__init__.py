"""Free Google Translate API for Python. Translates totally free of charge."""
__all__ = 'Translator',
__version__ = '4.0.0-rc.1'


from ext_libs.googletrans.client import Translator
from ext_libs.googletrans.constants import LANGCODES, LANGUAGES  # noqa
