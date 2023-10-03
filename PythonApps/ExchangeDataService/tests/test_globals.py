import os
from pathlib import Path
from modules import run_arg_cache as ar


ar.test_mode = True
ROOT_DIR = Path(os.path.dirname(os.path.abspath(__file__))).parents[0]
