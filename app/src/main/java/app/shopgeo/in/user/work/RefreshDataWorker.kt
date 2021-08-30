package app.shopgeo.`in`.user.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.shopgeo.`in`.user.database.ProductDatabase.Companion.getInstance
import app.shopgeo.`in`.user.repository.HomeRepository
import kotlinx.coroutines.InternalCoroutinesApi
import retrofit2.HttpException

class RefreshDataWorker (appContext : Context, params : WorkerParameters): CoroutineWorker(appContext,params){
    companion object {
        const val refreshHomeData = "app.shopgeo.`in`.user.RefreshDataWorker"
    }
    @InternalCoroutinesApi
    override suspend fun doWork() : Result{
        val database = getInstance(applicationContext)
        val repository = HomeRepository(database)
        try{
            repository.refreshProductsList()
        }catch (e : HttpException){
            return Result.retry()
        }
        return Result.success()
    }
}