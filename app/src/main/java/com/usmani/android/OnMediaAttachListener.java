package com.usmani.android;

import android.net.Uri;

public interface OnMediaAttachListener {	
	public void onMediaAttach(Uri uri,MediaType mType);
	public void onMediaAttachCancel();
}
