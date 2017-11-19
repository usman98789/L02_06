package Database.DatabaseDriver;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import Database.DatabaseDriver.PasswordHelpers;

import java.math.BigDecimal;

/**
 * Created by Joe on 2017-07-17.
 */

public class DatabaseDriverA extends SQLiteOpenHelper {
  private static final int DATABASE_VERSION = 1;
  private static final String DATABASE_NAME = "bank.db";

  public DatabaseDriverA(Context context) {
    super(context, DATABASE_NAME, null, 1);
  }

  @Override
  public void onCreate(SQLiteDatabase sqLiteDatabase) {
    sqLiteDatabase.execSQL("CREATE TABLE ROLES "
        + "(ID INTEGER PRIMARY KEY NOT NULL,"
        + "NAME TEXT NOT NULL)");
    sqLiteDatabase.execSQL("CREATE TABLE USERS "
            + "(ID INTEGER PRIMARY KEY NOT NULL,"
            + "NAME TEXT NOT NULL,"
            + "AGE INTEGER NOT NULL,"
            + "ADDRESS CHAR(100),"
            + "ROLEID INTEGER,"
            + "FOREIGN KEY(ROLEID) REFERENCES ROLE(ID))");
    sqLiteDatabase.execSQL("CREATE TABLE USERACCOUNT "
        + "(USERID INTEGER NOT NULL,"
        + "ACCOUNTID INTEGER NOT NULL,"
        + "FOREIGN KEY(USERID) REFERENCES USER(ID),"
        + "FOREIGN KEY(ACCOUNTID) REFERENCES ACOUNT(ID),"
        + "PRIMARY KEY(USERID, ACCOUNTID))");
    sqLiteDatabase.execSQL("CREATE TABLE USERPW "
        + "(USERID INTEGER NOT NULL,"
        + "PASSWORD CHAR(64),"
        + "FOREIGN KEY(USERID) REFERENCES USER(ID))");
    sqLiteDatabase.execSQL("CREATE TABLE MARK "
            + "(USERID INTEGER NOT NULL,"
            + "MARKS REAL NOT NULL,"
            + "FOREIGN KEY(USERID) REFERENCES USER(ID))");
    sqLiteDatabase.execSQL("CREATE TABLE USERMESSAGES "
            + "(ID INTEGER PRIMARY KEY NOT NULL,"
            + "USERID INTEGER NOT NULL,"
            + "MESSAGE CHAR(512) NOT NULL,"
            + "VIEWED CHAR(1) NOT NULL,"
            + "FOREIGN KEY(USERID) REFERENCES USER(ID))");
  }

  @Override
  public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
    sqLiteDatabase.execSQL("DROP TABLE IF EXISTS USERMESSAGES");
    sqLiteDatabase.execSQL("DROP TABLE IF EXISTS USERPW");
    sqLiteDatabase.execSQL("DROP TABLE IF EXISTS USERACCOUNT");
    sqLiteDatabase.execSQL("DROP TABLE IF EXISTS USERS");
    sqLiteDatabase.execSQL("DROP TABLE IF EXISTS ROLES");

    onCreate(sqLiteDatabase);
  }

  //INSERTS
  public long insertRole(String role) {
    SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
    ContentValues contentValues = new ContentValues();
    contentValues.put("NAME", role);
    return sqLiteDatabase.insert("ROLES", null, contentValues);
  }

  public long insertNewUser(String name, int age, String address, int roleId, String password) {
    long id = insertUser(name, age, address, roleId);
    insertPassword(password, (int) id);
    return id;
  }


  public long insertMessage(int userId, String message) {
    SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
    ContentValues contentValues = new ContentValues();
    contentValues.put("USERID", userId);
    contentValues.put("MESSAGE", message);
    contentValues.put("VIEWED", 0);
    return sqLiteDatabase.insert("USERMESSAGES", null, contentValues);
  }

  private long insertUser(String name, int age, String address, int roleId) {
    SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
    ContentValues contentValues = new ContentValues();
    contentValues.put("NAME", name);
    contentValues.put("AGE", age);
    contentValues.put("ADDRESS", address);
    contentValues.put("ROLEID", roleId);

    return sqLiteDatabase.insert("USERS", null, contentValues);
  }

  private void insertPassword(String password, int userId) {
    SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
    ContentValues contentValues = new ContentValues();

    password = PasswordHelpers.passwordHash(password);

    contentValues.put("USERID", userId);
    contentValues.put("PASSWORD", password);
    sqLiteDatabase.insert("USERPW", null, contentValues);
  }

  public void insertMark(int userId, double mark){
    SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
    ContentValues contentValues = new ContentValues();
    contentValues.put("USERID", userId);
    contentValues.put("MARKS", mark);
    sqLiteDatabase.insert("MARK", null, contentValues);
  }


  //SELECT METHODS
  protected double getMark(int userId) {
    SQLiteDatabase sqLiteDatabase = getReadableDatabase();
    Cursor cursor = sqLiteDatabase.rawQuery("SELECT MARKS FROM MARK WHERE USERID = ?",
            new String[]{String.valueOf(userId)});
    cursor.moveToFirst();
    double value = cursor.getDouble(cursor.getColumnIndex("MARKS"));
    cursor.close();
    return value;
  }

  protected Cursor getRoles() {
    SQLiteDatabase sqLiteDatabase = getReadableDatabase();
    return sqLiteDatabase.rawQuery("SELECT * FROM ROLES;", null);
  }

  protected String getRole(int id) {
    SQLiteDatabase sqLiteDatabase = getReadableDatabase();
    Cursor cursor = sqLiteDatabase.rawQuery("SELECT NAME FROM ROLES WHERE ID = ?",
        new String[]{String.valueOf(id)});
    cursor.moveToFirst();
    String value = cursor.getString(cursor.getColumnIndex("NAME"));
    cursor.close();
    return value;

  }

  protected int getUserRole(int userId) {
    SQLiteDatabase sqLiteDatabase = getReadableDatabase();
    Cursor cursor = sqLiteDatabase.rawQuery("SELECT ROLEID FROM USERS WHERE ID = ?",
        new String[]{String.valueOf(userId)});
    cursor.moveToFirst();
    int result = cursor.getInt(cursor.getColumnIndex("ROLEID"));
    cursor.close();
    return result;
  }

  protected Cursor getUsersDetails() {
    SQLiteDatabase sqLiteDatabase = getReadableDatabase();
    return sqLiteDatabase.rawQuery("SELECT * FROM USERS", null);
  }

  protected Cursor getUserDetails(int userId) {
    SQLiteDatabase sqLiteDatabase = getReadableDatabase();
    return sqLiteDatabase.rawQuery("SELECT * FROM USERS WHERE ID = ?",
        new String[]{String.valueOf(userId)});
  }

  protected String getPassword(int userId) {
    SQLiteDatabase sqLiteDatabase = getReadableDatabase();
    Cursor cursor = sqLiteDatabase.rawQuery("SELECT PASSWORD FROM USERPW WHERE USERID = ?",
        new String[]{String.valueOf(userId)});
    cursor.moveToFirst();
    String result = cursor.getString(cursor.getColumnIndex("PASSWORD"));
    cursor.close();
    return result;
  }


  protected Cursor getAllMessages(int userId) {
    SQLiteDatabase sqLiteDatabase = getReadableDatabase();
    return sqLiteDatabase.rawQuery("SELECT * FROM USERMESSAGES WHERE USERID = ?",
        new String[]{String.valueOf(userId)});
  }

  protected String getSpecificMessage(int messageId) {
    SQLiteDatabase sqLiteDatabase = getReadableDatabase();
    Cursor cursor = sqLiteDatabase.rawQuery("SELECT MESSAGE FROM USERMESSAGES WHERE ID = ?",
        new String[]{String.valueOf(messageId)});
    cursor.moveToFirst();
    String result = cursor.getString(cursor.getColumnIndex("MESSAGE"));
    cursor.close();
    return result;
  }

  //UPDATE Methods
  protected boolean updateRoleName(String name, int id) {
    SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
    ContentValues contentValues = new ContentValues();
    contentValues.put("NAME", name);
    return sqLiteDatabase.update("ROLES", contentValues, "ID = ?", new String[]{String.valueOf(id)})
        > 0;
  }

  protected boolean updateUserName(String name, int id) {
    SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
    ContentValues contentValues = new ContentValues();
    contentValues.put("NAME", name);
    return sqLiteDatabase.update("USERS", contentValues, "ID = ?", new String[]{String.valueOf(id)})
        > 0;
  }

  protected boolean updateUserAge(int age, int id) {
    SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
    ContentValues contentValues = new ContentValues();
    contentValues.put("AGE", age);
    return sqLiteDatabase.update("USERS", contentValues, "ID = ?", new String[]{String.valueOf(id)})
        > 0;
  }

  protected boolean updateUserRole(int roleId, int id) {
    SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
    ContentValues contentValues = new ContentValues();
    contentValues.put("ROLEID", roleId);
    return sqLiteDatabase.update("USERS", contentValues, "ID = ?", new String[]{String.valueOf(id)})
        > 0;
  }

  protected boolean updateUserAddress(String address, int id) {
    SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
    ContentValues contentValues = new ContentValues();
    contentValues.put("ADDRESS", address);
    return sqLiteDatabase.update("USERS", contentValues, "ID = ?", new String[]{String.valueOf(id)})
        > 0;
  }


  protected boolean updateUserPassword(String password, int id) {
    SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
    ContentValues contentValues = new ContentValues();
    contentValues.put("PASSWORD", password);
    return sqLiteDatabase.update("USERPW", contentValues, "USERID = ?",
        new String[]{String.valueOf(id)}) > 0;
  }

  protected boolean updateUserMessageState(int id) {
    SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
    ContentValues contentValues = new ContentValues();
    contentValues.put("VIEWED", 1);
    return sqLiteDatabase.update("USERMESSAGES", contentValues, "ID = ?",
        new String[]{String.valueOf(id)}) > 0;
  }
}
