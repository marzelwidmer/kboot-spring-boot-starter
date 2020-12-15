package hello

class ConsoleHelloService : HelloService {
    override fun sayHello() {
        println("-----> Hello from console! <-----")
    }
}
