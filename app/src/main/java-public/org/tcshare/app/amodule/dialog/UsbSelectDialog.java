package org.tcshare.app.amodule.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;


import org.tcshare.app.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @Description TODO
 * @Author B站：千古八方的玩具
 * @CreateTime 2025年10月28日 15:16:56
 */
public class UsbSelectDialog extends Dialog {

    private final OnSelectListener onSelectListener;
    private final Activity act;
    private AutoCompleteTextView acSelectBaund;
    private RadioGroup rgSerial;
    private final List<String> suggestionsBaud = new ArrayList<String>() {
        {
            add("115200");
            add("400000");
            add("416666");
            add("420000");
            add("460800");
            add("921600");
        }
    };
    public UsbSelectDialog(@NonNull Activity act, OnSelectListener onSelectListener) {
        super(act);
        this.act = act;
        this.onSelectListener = onSelectListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_device_select);
        rgSerial = findViewById(R.id.rgSerial);
        acSelectBaund = findViewById(R.id.acSelectBaund);

        acSelectBaund.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, suggestionsBaud));


        acSelectBaund.setThreshold(0);
        acSelectBaund.setOnClickListener(v -> acSelectBaund.showDropDown());
        acSelectBaund.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                acSelectBaund.showDropDown();
            }
        });


        refreshUSBDeviceList();

        findViewById(R.id.btnOK).setOnClickListener(v -> {
            int count = rgSerial.getChildCount();
            UsbDevice usbDevice = null;
            for (int i = 0; i < count; i++) {
                RadioButton child = (RadioButton) rgSerial.getChildAt(i);
                if (child.isChecked()) {
                    usbDevice = ((UsbDevice) child.getTag());
                    break;
                }
            }
            if (usbDevice == null) {
                Toast.makeText(getContext(), "请选择一个USB设备！", Toast.LENGTH_SHORT).show();
                return;
            }
            String serialBaud = acSelectBaund.getText().toString().trim();
            if (serialBaud.isEmpty()) {
                Toast.makeText(getContext(), "请选择一个通信频率！", Toast.LENGTH_SHORT).show();
            }


            onSelectListener.onSelect(UsbSelectDialog.this, usbDevice);

        });
        setCanceledOnTouchOutside(false);
    }

    public void refreshUSBDeviceList() {
        if (rgSerial != null) {
            rgSerial.removeAllViews();
            UsbManager usbManager = (UsbManager) act.getSystemService(Context.USB_SERVICE);
            HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
            for (UsbDevice d : deviceList.values()) {
                RadioButton child = (RadioButton) LayoutInflater.from(getContext()).inflate(R.layout.item_choice, null, false);
                child.setText(d.getProductName());
                child.setTag(d);
                rgSerial.addView(child);
            }
            rgSerial.invalidate();
        }
    }


    public interface OnSelectListener {

        void onSelect(UsbSelectDialog dialog, UsbDevice usbDevice);

    }
}
