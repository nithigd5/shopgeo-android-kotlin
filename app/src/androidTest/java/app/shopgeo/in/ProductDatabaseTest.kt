package app.shopgeo.`in`

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.shopgeo.`in`.user.database.ProductDatabase
import app.shopgeo.`in`.user.database.ProductsDao
import app.shopgeo.`in`.database.Product
import org.junit.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException


/**
 * This is not meant to be a full set of tests. For simplicity, most of your samples do not
 * include tests. However, when building the Room, it is helpful to make sure it works before
 * adding the UI.
 */

@RunWith(AndroidJUnit4::class)
class SleepDatabaseTest {

    private lateinit var ProductDao: ProductsDao
    private lateinit var db: ProductDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, ProductDatabase::class.java)
                // Allowing main thread queries, just for testing.
                .allowMainThreadQueries()
                .build()
        ProductDao = db.ProductsDao
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGet() {
        var list:List<String> = listOf("/ad","das","asddas")
        val product = Product(1,"title",list, 9.9F,10.0F,4.5F)

        ProductDao.Insert(product)
        val products = ProductDao.get(1)
        print(product)
        assertEquals("product Error: ${products}",products?.product_id,1)
    }
}