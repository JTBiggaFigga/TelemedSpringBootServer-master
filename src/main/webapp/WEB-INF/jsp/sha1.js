function sha1 (str) {

    function hex (value) {
        var s = (value >>> 0).toString(16)
        while (s.length < 8) s = '0' + s
        return s
    }

    function limit (n) {
        return n & 0xffffffff
    }

    function rotateLeft (n, s) {
        return (n << s) | (n >>> (32 - s))
    }

    var H0 = 0x67452301
    var H1 = 0xefcdab89
    var H2 = 0x98badcfe
    var H3 = 0x10325476
    var H4 = 0xc3d2e1f0

    var i, j
    var blockstart
    var W = new Array(80)
    var A, B, C, D, E
    var temp

    str = unescape(encodeURIComponent(str))
    str = str.split(/.*?/).map(function (s) {
        return s.charCodeAt(0)
    })

    var length = str.length

    var words = []
    for (i = 0; i < length - 3; i += 4) {
        j = str[i] << 24 | str[i + 1] << 16 | str[i + 2] << 8 | str[i + 3]
        words.push(j)
    }

    switch (length % 4) {
        case 0:
            i = 0x80000000
            break
        case 1:
            i = str[length - 1] << 24 | 0x800000
            break
        case 2:
            i = str[length - 2] << 24 | str[length - 1] << 16 | 0x8000
            break
        case 3:
            i = str[length - 3] << 24 | str[length - 2] << 16 | str[length - 1] << 8 | 0x80
            break
    }

    words.push(i)

    while ((words.length % 16) != 14) {
        words.push(0)
    }

    words.push(length >>> 29)
    words.push(limit(length << 3))

    for (blockstart = 0; blockstart < words.length; blockstart += 16) {

        for (i = 0; i < 16; i++) {
            W[i] = words[blockstart + i]
        }
        for (i = 16; i < 80; i++) {
            W[i] = rotateLeft(W[i - 3] ^ W[i - 8] ^ W[i - 14] ^ W[i - 16], 1)
        }

        A = H0
        B = H1
        C = H2
        D = H3
        E = H4

        for (i = 0; i <= 19; i++) {
            temp = limit(rotateLeft(A, 5) + ((B & C) | (~B & D)) + E + W[i] + 0x5a827999)
            E = D
            D = C
            C = rotateLeft(B, 30)
            B = A
            A = temp
        }

        for (i = 20; i <= 39; i++) {
            temp = limit(rotateLeft(A, 5) + (B ^ C ^ D) + E + W[i] + 0x6ed9eba1)
            E = D
            D = C
            C = rotateLeft(B, 30)
            B = A
            A = temp
        }

        for (i = 40; i <= 59; i++) {
            temp = limit(rotateLeft(A, 5) + ((B & C) | (B & D) | (C & D)) + E + W[i] + 0x8f1bbcdc)
            E = D
            D = C
            C = rotateLeft(B, 30)
            B = A
            A = temp
        }

        for (i = 60; i <= 79; i++) {
            temp = limit(rotateLeft(A, 5) + (B ^ C ^ D) + E + W[i] + 0xca62c1d6)
            E = D
            D = C
            C = rotateLeft(B, 30)
            B = A
            A = temp
        }

        H0 = limit(H0 + A)
        H1 = limit(H1 + B)
        H2 = limit(H2 + C)
        H3 = limit(H3 + D)
        H4 = limit(H4 + E)

    }

    return hex(H0) + hex(H1) + hex(H2) + hex(H3) + hex(H4)

}