import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: Text('Silent Auto Call Picker'),
        ),
        body: Center(
          child: CallHandlerButton(),
        ),
      ),
    );
  }
}

class CallHandlerButton extends StatefulWidget {
  @override
  _CallHandlerButtonState createState() => _CallHandlerButtonState();
}

class _CallHandlerButtonState extends State<CallHandlerButton> {
  static const platform = MethodChannel('com.example.silent_auto_call_picker/calls');

  Future<void> _initializeCallHandler() async {
    try {
      final String result = await platform.invokeMethod('initializeCallHandler');
      print(result);
    } on PlatformException catch (e) {
      print("Failed to initialize call handler: '${e.message}'.");
    }
  }

  @override
  Widget build(BuildContext context) {
    return ElevatedButton(
      onPressed: _initializeCallHandler,
      child: Text('Initialize Call Handler'),
    );
  }
}
