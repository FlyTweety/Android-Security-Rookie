import frida
import sys

# 定义要注入的Frida代码
js_code = """
if (Java.available) {
  Java.perform(function () {
    var ChildClass = Java.use("com.example.myapplication3.ChildClass"); //替换成aNetWorkCallFun()方法所在的类的名称
    ChildClass.aNetWorkCallFun.implementation = function (arg1) {
      var originalResult = this.aNetWorkCallFun(arg1); //调用原始方法获取结果
      var modifiedResult = "Modified Result"; //替换成您想要的新结果
      send("args: " + arg1);
      send("Original Result: " + originalResult);
      send("Modified Result: " + modifiedResult);
      return modifiedResult; //替换原始结果并返回
    };
  });
}

"""

# 定义应用程序包名
package_name = "com.example.myapplication3"

# 连接到设备并获取应用程序的进程ID
device = frida.get_usb_device()
pid = device.spawn([package_name])
session = device.attach(pid)

# 加载JavaScript代码
script = session.create_script(js_code)

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
