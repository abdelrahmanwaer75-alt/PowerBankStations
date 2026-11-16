import csv,sys
from pathlib import Path
import numpy as np
from src.processing import mean_filter_numpy
out=Path('experiments/synthetic_results.csv');out.parent.mkdir(exist_ok=True)
rng=np.random.default_rng(42);rows=[]
for sigma in (5,15,30):
 img=np.full((64,64),128,dtype=np.float32);noisy=np.clip(img+rng.normal(0,sigma,img.shape),0,255).astype(np.uint8);filtered=mean_filter_numpy(noisy,3)
 rows.append({'noise_sigma':sigma,'mse_before':float(np.mean((noisy.astype(float)-img)**2)),'mse_after':float(np.mean((filtered.astype(float)-img)**2))})
with out.open('w',newline='') as f:w=csv.DictWriter(f,fieldnames=rows[0].keys());w.writeheader();w.writerows(rows)
print(out)
