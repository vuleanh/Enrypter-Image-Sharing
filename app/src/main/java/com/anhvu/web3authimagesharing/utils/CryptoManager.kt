package com.anhvu.web3authimagesharing.utils

import android.content.Context
import android.content.ContextWrapper
import android.os.Environment
import android.security.keystore.KeyProperties
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.SecretKeySpec

class CryptoManager {

    fun encryptFile(context: Context, rawFile: File): File {
        val fis = FileInputStream(rawFile)
        val contextWrapper = ContextWrapper(context)

        val photoDir = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DCIM)
        val file = File(photoDir, "encfile" + ".png")
        val fos = FileOutputStream(file.path)

        val sks = SecretKeySpec("MyDifficultPassw".toByteArray(), ALGORITHM)

        val cipher = Cipher.getInstance(ALGORITHM)

        cipher.init(Cipher.ENCRYPT_MODE, sks)

        val cos = CipherOutputStream(fos, cipher)
        var b: Int
        val d = ByteArray(8)
        while (fis.read(d).also { b = it } != -1) {
            cos.write(d, 0, b)
        }
        cos.flush()
        cos.close()
        fis.close()
        return file
    }

    @Throws(
        IOException::class,
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class
    )
    fun decrypt(context: Context, encryptedFile: File, fileName: String) {

        val fis = FileInputStream(encryptedFile.path)

        val dir = File(context.filesDir, "decrypted")

        if (!dir.exists()) {
            dir.mkdir()
        }

        val decFile = File(dir, "$fileName.jpg")

        val fos = FileOutputStream(decFile.path)

        val sks = SecretKeySpec("MyDifficultPassw".toByteArray(), "AES")

        val cipher = Cipher.getInstance("AES")

        cipher.init(Cipher.DECRYPT_MODE, sks)

        val cis = CipherInputStream(fis, cipher)

        var b: Int
        val d = ByteArray(8)
        while (cis.read(d).also { b = it } != -1) {
            fos.write(d, 0, b)
        }

        fos.flush()
        fos.close()
        cis.close()

    }

    companion object {
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val TRANSFORMATION = KeyProperties.ENCRYPTION_PADDING_PKCS7
    }
}