package com.library.model;

import java.time.LocalDate;

/**
 * Represents a library reader (patron) in the Library Management System.
 *
 * <p>Maps to the {@code readers} table in the database:
 * <pre>
 *   id (PK) | full_name | email (UNIQUE) | phone | reg_date
 * </pre>
 *
 * <p>A reader may have zero or more associated {@link Loan} records.
 * Deletion of a reader is restricted by the database if any loans reference it.
 */
public class Reader {

    /** Primary key. Set by the database on INSERT. */
    private int       id;

    /** Full display name of the reader (NOT NULL in DB). */
    private String    fullName;

    /** Email address; nullable but UNIQUE when present. */
    private String    email;

    /** Phone number in any format; nullable. */
    private String    phone;

    /**
     * Registration date. Defaults to the current date in the database
     * ({@code DEFAULT CURRENT_DATE}); never {@code null} in practice.
     */
    private LocalDate regDate;

    /** No-arg constructor required for DAO result mapping. */
    public Reader() {}

    /**
     * Full constructor used by {@code ReaderDaoImpl} when mapping a {@link java.sql.ResultSet}.
     *
     * @param id       primary key
     * @param fullName full display name
     * @param email    email address (may be {@code null})
     * @param phone    phone number (may be {@code null})
     * @param regDate  registration date
     */
    public Reader(int id, String fullName, String email,
                  String phone, LocalDate regDate) {
        this.id       = id;
        this.fullName = fullName;
        this.email    = email;
        this.phone    = phone;
        this.regDate  = regDate;
    }

    // ── Getters ───────────────────────────────────────────────────

    /** @return primary key of this reader */
    public int       getId()       { return id; }

    /** @return full display name */
    public String    getFullName() { return fullName; }

    /** @return email address, or {@code null} */
    public String    getEmail()    { return email; }

    /** @return phone number, or {@code null} */
    public String    getPhone()    { return phone; }

    /** @return registration date (never {@code null} in practice) */
    public LocalDate getRegDate()  { return regDate; }

    // ── Setters ───────────────────────────────────────────────────

    /** @param id primary key (assigned after INSERT) */
    public void setId(int id)              { this.id = id; }

    /** @param n full display name (must not be blank) */
    public void setFullName(String n)      { this.fullName = n; }

    /** @param email email address; {@code null} is allowed */
    public void setEmail(String email)     { this.email = email; }

    /** @param phone phone number; {@code null} is allowed */
    public void setPhone(String phone)     { this.phone = phone; }

    /** @param d registration date */
    public void setRegDate(LocalDate d)    { this.regDate = d; }
}