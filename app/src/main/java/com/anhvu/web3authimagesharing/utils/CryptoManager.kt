package com.anhvu.web3authimagesharing.utils

import android.content.Context
import android.content.ContextWrapper
import android.os.Environment
import android.security.keystore.KeyProperties
import android.util.Log
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

//    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
//        load(null)
//    }
//
//    private val encryptCipher = Cipher.getInstance(TRANSFORMATION).apply {
//        init(Cipher.ENCRYPT_MODE, getKey())
//    }

//    private fun getKey(): SecretKey {
//        val existingKey = keyStore.getEntry("secret", null) as? KeyStore.SecretKeyEntry
//        return existingKey?.secretKey ?: createKey()
//    }
//
//    private fun createKey(): SecretKey {
//
//        return KeyGenerator.getInstance(ALGORITHM).apply {
//            init(
//                KeyGenParameterSpec.Builder(
//                    "secret",
//                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
//                ).setBlockModes(BLOCK_MODE).build()
//            )
//        }.generateKey()
//    }

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
        // on below line creating and initializing variable for context wrapper.
        val contextWrapper = ContextWrapper(context)

        // on below line creating a file for getting photo directory.
//        val photoDir = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DCIM)

        // on below line creating a new file for encrypted image.
//        val file = File(photoDir, "encfile" + ".png")

        // on below line creating input stream for file with file path.
//        val fis = FileInputStream(file.path)

        val fis = FileInputStream(encryptedFile.path)

        val dir = File(context.filesDir, "decrypted")

        if (!dir.exists()) {
            dir.mkdir()
        }

        // on below line creating a file for decrypted image.
        val decFile = File(dir, "$fileName.jpg")

        // on below line creating an file output stream for decrypted image.
        val fos = FileOutputStream(decFile.path)

        // creating a variable for secret key and passing our secret key
        // and algorithm for encryption.
        val sks = SecretKeySpec("MyDifficultPassw".toByteArray(), "AES")

        // on below line creating a variable
        // for cipher and initializing it
        val cipher = Cipher.getInstance("AES")

        // on below line initializing cipher and
        // specifying decrypt mode to decrypt.
        cipher.init(Cipher.DECRYPT_MODE, sks)

        // on below line creating a variable for cipher input stream.
        val cis = CipherInputStream(fis, cipher)

        // on below line creating a variable b.
        var b: Int
        val d = ByteArray(8)
        while (cis.read(d).also { b = it } != -1) {
            fos.write(d, 0, b)
        }

        // on below line flushing our fos,
        // closing fos and closing cis.
        fos.flush()
        fos.close()
        cis.close()

        // displaying toast message.
        Log.d("AAAA", "File decrypted successfully..")

        // on below line creating an image file
        // from decrypted image file path.
//        val imgFile = File(decFile.path)
//        if (imgFile.exists()) {
//            // creating bitmap for image and displaying
//            // that bitmap in our image view.
//            val bitmap = BitmapFactory.decodeFile(imgFile.path)
//            imageView.setImageBitmap(bitmap)
//        }
    }

    companion object {
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val TRANSFORMATION = KeyProperties.ENCRYPTION_PADDING_PKCS7
    }
}