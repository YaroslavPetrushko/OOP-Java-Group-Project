package com.library.model;

/**
 * Represents an author entity in the Library Management System.
 *
 * <p>Maps to the {@code authors} table in the database:
 * <pre>
 *   id (PK) | full_name | country | birth_year
 * </pre>
 *
 * <p>An author may have zero or more associated {@link Book} records.
 * Deletion of an author is restricted by the database if any books reference it.
 */
public class Author {

    /** Primary key. Set by the database on INSERT. */
    private int    id;

    /** Full display name of the author (NOT NULL in DB). */
    private String fullName;

    /** Country of origin; nullable. */
    private String country;

    /**
     * Year of birth; {@code 0} is used as a sentinel for "not set"
     * (stored as NULL in the database).
     */
    private int    birthYear;

    /** No-arg constructor required for DAO result mapping. */
    public Author() {}

    /**
     * Full constructor used by {@code AuthorDaoImpl} when mapping a {@link java.sql.ResultSet}.
     *
     * @param id        primary key
     * @param fullName  full name of the author
     * @param country   country of origin (may be {@code null})
     * @param birthYear birth year, or {@code 0} if unknown
     */
    public Author(int id, String fullName, String country, int birthYear) {
        this.id        = id;
        this.fullName  = fullName;
        this.country   = country;
        this.birthYear = birthYear;
    }

    // ── Getters ───────────────────────────────────────────────────

    /** @return primary key of this author */
    public int    getId()        { return id; }

    /** @return full display name */
    public String getFullName()  { return fullName; }

    /** @return country of origin, or {@code null} */
    public String getCountry()   { return country; }

    /**
     * @return birth year, or {@code 0} if not set
     */
    public int    getBirthYear() { return birthYear; }

    // ── Setters ───────────────────────────────────────────────────

    /** @param id primary key (assigned after INSERT) */
    public void setId(int id)              { this.id = id; }

    /** @param n full display name (must not be blank) */
    public void setFullName(String n)      { this.fullName = n; }

    /** @param country country of origin; {@code null} is allowed */
    public void setCountry(String country) { this.country = country; }

    /**
     * @param y birth year; pass {@code 0} to store NULL in the database
     */
    public void setBirthYear(int y)        { this.birthYear = y; }
}