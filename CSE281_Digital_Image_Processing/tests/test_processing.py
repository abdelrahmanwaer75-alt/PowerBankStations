import numpy as np
from src.processing import mean_filter_numpy,otsu_numpy,analyze_image
def test_mean_filter_shape():
 a=np.arange(25,dtype=np.uint8).reshape(5,5);assert mean_filter_numpy(a,3).shape==a.shape
def test_otsu_range():
 a=np.zeros((10,10),np.uint8);a[:,5:]=255;assert 0<=otsu_numpy(a)<=255
def test_pipeline():
 a=np.zeros((40,40,3),np.uint8);r=analyze_image(a);assert r['kind']=='powerbank' and 'status' in r
