package bar

class BarServiceImpl : BarService{
     override fun sayBye() {
        println("-----> BarService say bye from starter <-----")
    }
}

interface BarService {
    fun sayBye()
}
