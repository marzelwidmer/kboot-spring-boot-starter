package foo

class FooServiceImpl : FooService{
     override fun sayFoo() {
        println("-----> Foo from starter <-----")
    }
}

interface FooService {
    fun sayFoo()
}
