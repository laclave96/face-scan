package com.savent.recognition.face.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.savent.recognition.face.data.local.database.dao.CompanyDao
import com.savent.recognition.face.data.local.database.dao.EmployeeDao
import com.savent.recognition.face.data.local.model.CompanyEntity
import com.savent.recognition.face.data.local.model.EmployeeEntity
import com.savent.recognition.face.utils.Converters

@Database(
    entities = [EmployeeEntity::class, CompanyEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun employeeDao(): EmployeeDao
    abstract fun companyDao(): CompanyDao
    /*companion object {
        @Volatile
        private var sINSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            return sINSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )   .addCallback(object:Callback(){
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        }
                    }
                })
                    .build()
                sINSTANCE = instance
                return instance
            }
        }
    }*/
}