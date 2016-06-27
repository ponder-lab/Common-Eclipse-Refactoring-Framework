## Cycle Error

If you get this error:

```
A cycle was detected when generating the classpath ...
```

That looks something like this:

```
A cycle was detected when generating the classpath: 

edu.cuny.citytech.refactoring.common.tests_1.5.0.201606271049, 
org.eclipse.jdt.ui.tests.refactoring_3.10.100.v20150330-1509, 
*org.eclipse.jdt.ui_3.11.2.v20151123-1510, 
edu.cuny.citytech.defaultrefactoring.core_1.1.0, 
*org.eclipse.jdt.ui_3.11.2.v20151123-1510.
```

Close the org.eclipse.jdt plug-in project in the workspace. Then, rebuild.