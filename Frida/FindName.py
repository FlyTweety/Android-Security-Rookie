import frida

process = frida.get_usb_device().enumerate_processes()
for p in process:
    print(p)