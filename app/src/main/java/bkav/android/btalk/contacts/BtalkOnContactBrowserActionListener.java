package bkav.android.btalk.contacts;


import android.net.Uri;

import com.android.contacts.list.OnContactBrowserActionListener;

/**
 * Interface thêm sự kiện khi bấm vào item contact.
 */
public interface BtalkOnContactBrowserActionListener extends OnContactBrowserActionListener {
    /**
     * AnhNDd: Nếu có một số điện thoại thì gọi trực tiếp, không thì hiển thị danh sách các số đó lên.
     */
    void showPhoneNumberOrCall(Uri contactLookupUri, boolean isEnterpriseContact);
}
