# AirBnb Backend API

## Project Overview

AirBnb Backend is a production-grade Spring Boot application that provides a comprehensive REST API for hotel booking and property management. The system enables hotel managers to list and manage properties while allowing guests to browse hotels, manage bookings, and process payments through integrated payment processing.

The platform addresses the need for a scalable, secure, and reliable accommodation booking system with support for multi-user roles, dynamic pricing, real-time inventory management, and payment processing.

## Key Features

- **JWT-based Authentication & Authorization**: Stateless authentication with access and refresh tokens stored in HTTP-only cookies
- **Role-Based Access Control**: Two distinct roles - GUEST and HOTEL_MANAGER with endpoint-level authorization
- **Hotel Management**: CRUD operations for hotel properties with activation/deactivation capabilities
- **Room Management**: Manage multiple room types per hotel with capacity and pricing configuration
- **Dynamic Inventory System**: Real-time room availability tracking with surge pricing based on demand
- **Booking Workflow**: Multi-step booking process with guest management, payment initiation, and cancellation
- **Stripe Payment Integration**: Secure payment processing with webhook support for transaction confirmation
- **Secure REST APIs**: Role-based endpoint protection with global exception handling
- **Stateless Backend Design**: Containerized, horizontally scalable architecture using JWT for session management
- **Soft Deletion Strategy**: Logical deletion support for hotels with activation/deactivation workflow

## Tech Stack

- **Backend Framework**: Spring Boot 4.0.1
- **Java Version**: Java 21
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA with Hibernate
- **Security**: Spring Security with JWT (JJWT 0.13.0)
- **Build Tool**: Maven
- **Password Encoding**: BCrypt
- **DTO Mapping**: ModelMapper 3.2.6
- **Payment Processing**: Stripe Java SDK 31.3.0
- **Dependency Injection**: Lombok
- **Development Tools**: Spring DevTools

## System Architecture

### Layered Architecture

The application follows a three-tier layered architecture pattern:

```
Controller Layer
    ↓
Service Layer (Business Logic)
    ↓
Repository Layer (Data Access)
    ↓
Database (PostgreSQL)
```

### Component Breakdown

**Controllers** (`/controllers`)
- `AuthController`: Handles signup, login, and token refresh operations
- `HotelAdminController`: CRUD operations for hotels (HOTEL_MANAGER only)
- `RoomsAdminController`: Room management for hotel managers
- `HotelBrowseController`: Public API for searching and viewing hotels
- `HotelBookingController`: Booking lifecycle management
- `WebhookController`: Stripe webhook integration for payment confirmation

**Services** (`/service`)
- Interface-based design (`IHotelService`, `IBookingService`, `IInventoryService`, `IRoomService`)
- Concrete implementations (`CHotelService`, `CBookingService`, `CInventoryService`, `CRoomService`)
- `AuthService`: Authentication logic and token management
- `CheckoutService`: Payment initiation and session management
- `UserService`: User profile management
- `PricingUpdateService`: Dynamic pricing calculations

**Repositories** (`/repository`)
- JPA repositories for all entities with custom query methods
- Pagination and filtering support for complex searches

**DTOs** (`/dto`)
- Separation of concerns between API contracts and domain entities
- Includes: `HotelDTO`, `RoomDTO`, `BookingDTO`, `UserDTO`, `GuestDTO`, `LoginDTO`, `SignUpRequestDTO`

**Security Flow**
1. User credentials validated via Spring Security's `AuthenticationManager`
2. BCrypt-hashed passwords compared during authentication
3. JWT access token (10-minute expiration) and refresh token (180-day expiration) generated
4. Refresh token stored in HTTP-only, secure cookie (prevents XSS attacks)
5. Access token included in Authorization header for subsequent requests
6. `JWTAuthFilter` validates token signature and user roles
7. Role-based authorization enforced at controller method level

**Inventory Management**
- `Inventory` entity tracks daily room availability per room type
- Fields: `bookedCount`, `reservedCount`, `totalCount`, `surgeFactor`
- Supports search queries filtering by date range, city, and capacity
- Dynamic pricing applied based on demand (surge factor)

## Database Design

### Entity Relationships

**User**
- Central entity representing application users
- Implements Spring's `UserDetails` interface for security integration
- Roles stored as enum collection (GUEST, HOTEL_MANAGER)
- One-to-Many relationship with Hotels (as owner)

**Hotel**
- Property managed by a HOTEL_MANAGER user
- Fields: `name`, `city`, `photos[]`, `amenities[]`, `isActive`, `hotelContactInfo`
- Timestamps: `createdAt`, `updatedAt`
- One-to-Many relationship with Rooms and Inventory
- Soft deletion through `isActive` flag

**Room**
- Specific room type within a hotel
- Fields: `type`, `basePrice`, `photos[]`, `amenities[]`, `capacity`, `totalCount`
- Many-to-One relationship with Hotel
- One-to-Many relationship with Inventory

**Inventory**
- Daily availability tracking per room per hotel
- Unique constraint: (hotel_id, room_id, date)
- Fields: `bookedCount`, `reservedCount`, `totalCount`, `surgeFactor`
- Supports efficient date-range queries

**Booking**
- Represents a confirmed or in-progress reservation
- Fields: `checkInDate`, `checkOutDate`, `roomsCount`, `totalPrice`, `bookingStatus`
- Status progression: RESERVED → GUESTS_ADDED → PAYMENT_PENDING → CONFIRMED / CANCELLED
- Many-to-Many relationship with Guests
- Foreign keys: `hotel_id`, `room_id`, `user_id`

**Guest**
- Individual occupants of a booking
- Fields: `name`, `email`, `gender`, `age`, `countryCode`
- Many-to-Many relationship with Bookings via `BookingGuest` junction table

**HotelContactInfo**
- Embedded entity within Hotel
- Fields: `phoneNumber`, `address`, `city`

**HotelMinPrice**
- Denormalized entity for search optimization
- Stores minimum room price per hotel for efficient filtering

## Authentication & Authorization Flow

### Signup & Login

**Signup Flow**
1. User submits email, password, and name via `POST /auth/signup`
2. System checks for existing email (prevents duplicates)
3. Password encoded using BCrypt algorithm
4. User created with GUEST role by default
5. Returns `UserDTO` with user ID and email

**Login Flow**
1. User submits email and password via `POST /auth/login`
2. `AuthenticationManager` validates credentials against BCrypted password
3. JWT tokens generated:
   - **Access Token**: 10-minute validity, contains user ID and roles
   - **Refresh Token**: 180-day validity, minimal claims
4. Refresh token placed in HTTP-only cookie
5. Access token returned in response body

### Token Management

**Access Token**
- Claims: `subject` (user ID), `email`, `roles`, `issuedAt`, `expiration`
- Signed with HS256 using configured JWT secret key
- Included in `Authorization: Bearer <token>` header
- Validation occurs in `JWTAuthFilter` before controller execution

**Refresh Token**
- Minimal claims: `subject` (user ID), `issuedAt`, `expiration`
- Persisted in HTTP-only secure cookie
- Endpoint: `POST /auth/refresh` generates new access token
- Prevents re-authentication for 180 days without password entry

### Role Enforcement

**HOTEL_MANAGER**
- Access: `/admin/**` endpoints (hotel and room management)
- Can create, update, delete hotels and rooms
- Can activate deactivated hotels

**GUEST**
- Access: `/bookings/**` endpoints (booking operations)
- Can search hotels via `/hotels/search`
- Can view hotel details via `/hotels/{hotelId}/info`
- Cannot access admin endpoints

**Public Access**
- `/auth/signup`, `/auth/login`, `/auth/refresh`: No authentication required
- `/hotels/search`, `/hotels/{hotelId}/info`: Accessible without authentication

### Library Scoping

Hotel ownership is enforced through the booking and room management flow:
- Rooms are scoped to specific hotels
- Inventory is unique per hotel-room-date combination
- Bookings tied to specific hotels
- Hotel activation/deactivation controls visibility in search results

## API Overview

### Authentication APIs

- **POST** `/auth/signup` - Register new user account
  - Body: `SignUpRequestDTO` (email, password, name)
  - Returns: `UserDTO` with user details
  - Status: 201 Created

- **POST** `/auth/login` - Authenticate and obtain tokens
  - Body: `LoginDTO` (email, password)
  - Returns: `LoginResponseDTO` with access token
  - Sets refresh token in HTTP-only cookie
  - Status: 200 OK

- **POST** `/auth/refresh` - Obtain new access token
  - Reads refresh token from cookie
  - Returns: `LoginResponseDTO` with new access token
  - Status: 200 OK

### Hotel Admin APIs (requires HOTEL_MANAGER role)

- **POST** `/admin` - Create new hotel
  - Body: `HotelDTO` with hotel details
  - Returns: Created `HotelDTO`
  - Status: 201 Created

- **GET** `/admin/{hotelId}` - Retrieve hotel by ID
  - Returns: `HotelDTO`
  - Status: 200 OK

- **PUT** `/admin/{hotelId}` - Update hotel details
  - Body: `HotelDTO` with updated fields
  - Returns: Updated `HotelDTO`
  - Status: 200 OK

- **DELETE** `/admin/{hotelId}` - Delete hotel (soft delete)
  - Sets `isActive` to false
  - Status: 204 No Content

- **PATCH** `/admin/{hotelId}` - Activate deactivated hotel
  - Sets `isActive` to true
  - Status: 204 No Content

### Room Admin APIs (requires HOTEL_MANAGER role)

- **POST** `/admin/rooms` - Create room for hotel
  - Body: `RoomDTO` with room configuration
  - Returns: Created `RoomDTO`
  - Status: 201 Created

- **PUT** `/admin/rooms/{roomId}` - Update room details
  - Body: `RoomDTO` with updated fields
  - Returns: Updated `RoomDTO`
  - Status: 200 OK

### Hotel Browse APIs (public)

- **GET** `/hotels/search` - Search available hotels
  - Body: `HotelSearchRequest` (city, checkInDate, checkOutDate, guestCount, roomCount)
  - Returns: `Page<HotelPriceDTO>` with paginated results
  - Includes minimum price per hotel with surge pricing
  - Status: 200 OK

- **GET** `/hotels/{hotelId}/info` - Get hotel details
  - Returns: `HotelInfoDTO` with rooms, amenities, contact info
  - Status: 200 OK

### Booking APIs (requires authentication)

- **POST** `/bookings/init` - Initialize booking
  - Body: `BookingRequestDTO` (hotelId, roomId, checkInDate, checkOutDate, roomsCount)
  - Returns: `BookingDTO` with initial status RESERVED
  - Status: 200 OK

- **POST** `/bookings/{bookingId}/addGuests` - Add guest information
  - Body: List of `GuestDTO` objects
  - Returns: Updated `BookingDTO` with status GUESTS_ADDED
  - Status: 200 OK

- **POST** `/bookings/{bookingId}/payments` - Initiate payment
  - Returns: `sessionUrl` for Stripe checkout
  - Updates booking status to PAYMENT_PENDING
  - Status: 200 OK

- **POST** `/bookings/{bookingId}/cancel` - Cancel booking
  - Updates booking status to CANCELLED
  - Releases reserved inventory
  - Status: 204 No Content

### Webhook APIs

- **POST** `/webhooks/stripe` - Handle Stripe payment confirmation
  - Validates webhook signature
  - Updates booking status to CONFIRMED
  - Updates inventory with booked count

## Environment Setup

### Required Tools

- **Java 21**: JDK installation for running Spring Boot application
- **Maven 3.8+**: Build automation and dependency management
- **PostgreSQL 13+**: Relational database server
- **Stripe Account**: For payment processing (test mode for development)

### Environment Variables Configuration

Create a `.env` file in the project root or configure system environment variables:

```
DB_USERNAME=your_postgres_username
DB_PASSWORD=your_postgres_password
JWT_SECRET=your_256bit_secret_key_minimum_32_characters
FRONTEND_URL=http://localhost:3000
STRIPE_SECRET_KEY=sk_test_xxxxx
STRIPE_WEBHOOK_SECRET=whsec_xxxxx
```

### Properties Configuration

**application-local.properties** (development environment)
- Overrides values in `application.properties`
- Includes database credentials and local service URLs
- Not committed to version control

**application.properties** (shared configuration)
- Database connection string template with environment variables
- JPA/Hibernate settings (DDL auto-update, SQL formatting)
- API context path `/api/v1`
- Stripe configuration references
- Active profile: `local`

### Security Best Practices

All sensitive configuration values are externalized:
- Database credentials injected at runtime
- JWT secret key never hardcoded
- Stripe API keys stored as environment variables
- Frontend URL configured per deployment environment

## Running the Application

### Prerequisites

Ensure PostgreSQL is running on `localhost:5432` with an empty database named `airBnb`:

```bash
# Create database (if not exists)
createdb airBnb
```

### Step-by-Step Execution

1. **Clone the Repository**
   ```bash
   git clone <repository-url>
   cd AirBnb_SpringBoot
   ```

2. **Configure Environment Variables**
   ```bash
   # Create .env file or export environment variables
   export DB_USERNAME=postgres
   export DB_PASSWORD=your_password
   export JWT_SECRET=your_secure_256bit_key_minimum_32_chars
   export FRONTEND_URL=http://localhost:3000
   export STRIPE_SECRET_KEY=sk_test_xxxxx
   export STRIPE_WEBHOOK_SECRET=whsec_xxxxx
   ```

3. **Build the Application**
   ```bash
   mvn clean install
   ```

4. **Run the Application**
   ```bash
   mvn spring-boot:run
   ```
   
   Application starts on `http://localhost:8080/api/v1`

5. **Verify Health**
   ```bash
   curl http://localhost:8080/api/v1/auth/signup
   # Should fail with 400 (missing body) but confirm server is running
   ```

### Database Schema Initialization

Hibernate auto-creates database schema based on entity mappings:
- `spring.jpa.hibernate.ddl-auto=update` enables automatic schema updates
- Tables created on first application startup
- Existing schema preserved on subsequent runs

### Development Mode

DevTools enables fast restart during development:
```bash
mvn spring-boot:run
# Automatic restart on file changes in src/ directory
```

## Security Best Practices Implemented

**JWT Authentication**
- Stateless token-based authentication eliminates server-side session storage
- HS256 signing algorithm with configurable secret key
- Token expiration enforced by token validation in filters

**Password Security**
- BCrypt password encoding with configurable strength (default rounds = 10)
- Passwords never logged or returned in API responses
- Original passwords discarded after encoding

**Stateless Sessions**
- No HttpSession created by application
- `SessionCreationPolicy.STATELESS` configured in security filter chain
- Enables horizontal scaling without session synchronization

**Role-Based Access Control**
- Declarative authorization via `@PreAuthorize` and `hasRole()` expressions
- Endpoint-level protection for admin operations
- Global access denied handler for unauthorized requests

**Sensitive Configuration**
- All secrets externalized to environment variables
- No credentials committed to version control
- Local configuration files ignored via `.gitignore`
- Stripe webhook signature validation prevents unauthorized requests

**HTTP Security Headers**
- CSRF protection disabled for stateless REST APIs (JWT doesn't require it)
- HTTP-only cookies for refresh tokens prevent JavaScript access
- Secure flag set on cookies in production

**Exception Handling**
- Global exception handler prevents information disclosure
- Stack traces never exposed to API clients in production
- Validation errors return sanitized messages

## Future Enhancements

- **Advanced Booking Workflow**: Multi-night reservation holds, waitlist management, and booking modifications
- **Pagination & Advanced Filtering**: Cursor-based pagination, faceted search by amenities, price ranges, ratings
- **Rate Limiting**: Token bucket algorithm for API rate limiting by user and IP
- **Audit Logging**: Comprehensive audit trail for booking changes, payment events, and admin operations
- **Dockerization**: Docker container support with docker-compose for local development
- **Test Coverage Expansion**: Unit tests for services, integration tests for API endpoints, contract testing with consumers
- **Caching Layer**: Redis caching for hotel search results and inventory data
- **Internationalization**: Multi-language support for error messages and API responses
- **Analytics & Reporting**: Booking trends, revenue analytics, occupancy rates per hotel
- **Dispute Resolution**: Booking cancellation policies, refund workflows, customer support ticket system
- **Performance Optimization**: Database query optimization with composite indexes, search query optimization
- **Message Queue Integration**: Asynchronous booking confirmation emails via RabbitMQ or Kafka

## Contributing

Contributions are welcome. Please follow the existing code structure:
- Interface-based service design
- DTO usage for API contracts
- Global exception handling
- Comprehensive commit messages

## Author

Priyanshu Kumar

GitHub: [https://github.com/priyanshu886291kumar](https://github.com/priyanshu886291kumar)
