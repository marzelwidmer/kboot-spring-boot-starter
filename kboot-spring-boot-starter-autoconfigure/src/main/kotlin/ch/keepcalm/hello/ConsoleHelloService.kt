package ch.keepcalm.hello

class ConsoleHelloService : HelloService {
    override fun sayHello() {
        println("-----> Hello from console! <-----")
    }
}
