import frida
import sys

# 定义要注入的Frida代码
js_code = """
if (Java.available) {
  Java.perform(function () {
        
    // send(JSON.stringify(Java.enumerateMethods("*!setRequestMethod")));

    // hook functions
    %s
  });
}
"""

# 定义应用程序包名
package_name = "com.example.myapplication3"
#package_name = "com.adobe.flash13"

# 加载函数列表文件a.txt
with open('HookList.txt', 'r') as f:
    functions = f.readlines()

# 构造JavaScript代码，用于hook函数
hook_code = ""
for func in functions:
    class_name, method_name, *args = func.strip().split()
    shortClsName = class_name.split(".")[-1]
    argFlag = 0
    if args:
        argFlag = 1
        arg_list = ', '.join(args)
    else:
        argFlag = 0
        arg_list = 'noarg'

    if(argFlag == 1):
        hook_code += f"""
        var {shortClsName} = Java.use('{class_name}');
        if({shortClsName}) send('find' + {shortClsName});
        {shortClsName}.{method_name}.implementation = function ({arg_list}) {{
            var originalResult = this.{method_name}({arg_list});
            var modifiedResult = 'Modified Result';
            send('Class: {class_name}, Method: {method_name}, Args: ' + {arg_list});
            send('Original Result: ' + originalResult);
            send('Modified Result: ' + modifiedResult);
            return originalResult;
        }};
        """
    else:
        hook_code += f"""
        var {shortClsName} = Java.use('{class_name}');
        {shortClsName}.{method_name}.implementation = function () {{
            var originalResult = this.{method_name}();
            var modifiedResult = 'Modified Result';
            send('Class: {class_name}, Method: {method_name}, NoArgs');
            send('Original Result: ' + originalResult);
            send('Modified Result: ' + modifiedResult);
            return originalResult;
        }};
        """
# 连接到设备并获取应用程序的进程ID
device = frida.get_usb_device()
pid = device.spawn([package_name])
session = device.attach(pid)

print(js_code % hook_code)

# 加载JavaScript代码
script = session.create_script(js_code % hook_code)

# 定义处理函数，用于处理从Frida代码发送到客户端的消息
def on_message(message, data):
    print(message)

# 启动脚本并将处理函数分配给on_message回调
script.on('message', on_message)
script.load()

# 恢复应用程序的运行
device.resume(pid)

# 等待脚本结束
sys.stdin.read()
