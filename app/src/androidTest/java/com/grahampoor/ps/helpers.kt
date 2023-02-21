package com.grahampoor.ps
fun factorial(n: Int): Long {
    if (n < 0) {
        throw IllegalArgumentException("n must be a non-negative integer")
    }

    var result = 1L
    for (i in 1..n) {
        result *= i.toLong()
    }

    return result
}
