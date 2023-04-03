if (Java.available) {

  Java.perform(function () {
  var ChildClass = Java.use("com.example.myapplication3.ChildClass"); 
  ChildClass.aNetWorkCallFun.implementation = function (arg1) {
      var originalResult = this.aNetWorkCallFun(arg1); 
      var modifiedResult = "Modified Result"; 
      send("args: " + arg1);
      send("Original Result: " + originalResult);
      send("Modified Result: " + modifiedResult);
      return modifiedResult; 
    };
  });
  
}



