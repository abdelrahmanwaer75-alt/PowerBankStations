import cv2, numpy as np
def mean_filter_numpy(image:np.ndarray,k:int=3)->np.ndarray:
    if k<1 or k%2==0:raise ValueError('kernel size must be positive odd')
    p=k//2; a=np.pad(image.astype(float),((p,p),(p,p)),mode='reflect'); out=np.empty(image.shape,float)
    for y in range(image.shape[0]):
        for x in range(image.shape[1]):out[y,x]=a[y:y+k,x:x+k].mean()
    return np.clip(out,0,255).astype(np.uint8)
def otsu_numpy(gray:np.ndarray)->int:
    hist=np.bincount(gray.ravel(),minlength=256).astype(float);total=gray.size;sum_total=np.dot(np.arange(256),hist);sum_b=0.;w_b=0.;best=-1.;threshold=0
    for t in range(256):
        w_b+=hist[t]
        if w_b==0:continue
        w_f=total-w_b
        if w_f==0:break
        sum_b+=t*hist[t];m_b=sum_b/w_b;m_f=(sum_total-sum_b)/w_f;var=w_b*w_f*(m_b-m_f)**2
        if var>best:best=var;threshold=t
    return threshold
def analyze_image(image:np.ndarray,kind:str='powerbank'):
    if image is None or image.size==0:raise ValueError('empty image')
    gray=cv2.cvtColor(image,cv2.COLOR_BGR2GRAY) if image.ndim==3 else image.copy()
    enhanced=cv2.createCLAHE(clipLimit=2.0,tileGridSize=(8,8)).apply(gray)
    blur=cv2.GaussianBlur(enhanced,(3,3),0)
    if kind=='infrastructure':
        edges=cv2.Canny(blur,35,110);mask=cv2.morphologyEx(edges,cv2.MORPH_CLOSE,np.ones((3,3),np.uint8))
    elif kind=='pcb':
        edges=cv2.Laplacian(blur,cv2.CV_8U);_,mask=cv2.threshold(edges,0,255,cv2.THRESH_BINARY+cv2.THRESH_OTSU)
    else:
        edges=cv2.Canny(blur,45,130);mask=cv2.morphologyEx(edges,cv2.MORPH_OPEN,np.ones((3,3),np.uint8))
    count,labels,stats,centroids=cv2.connectedComponentsWithStats((mask>0).astype(np.uint8),8)
    regions=[]
    for i in range(1,count):
        x,y,w,h,area=map(int,stats[i])
        if area>=5:regions.append({'x':x,'y':y,'width':w,'height':h,'area_pixels':area})
    return {'kind':kind,'status':'POSSIBLE_VISIBLE_DEFECT' if regions else 'NO_CANDIDATE_DETECTED','image_width':int(gray.shape[1]),'image_height':int(gray.shape[0]),'candidate_regions':regions,'candidate_area_pixels':int(sum(r['area_pixels'] for r in regions)),'mask':mask}
def iou_score(pred,truth):
    p=pred>0;t=truth>0;union=np.logical_or(p,t).sum();return float(np.logical_and(p,t).sum()/union) if union else 1.0
