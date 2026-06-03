# Import Order Assignment System

## 1. Giới thiệu project

Import Order Assignment System là ứng dụng desktop JavaFX dùng để tin học hóa quy trình đặt hàng nhập khẩu của một công ty kinh doanh hàng nhập ngoại.

Trong quy trình thủ công, bộ phận kinh doanh tạo danh sách các mặt hàng cần nhập. Sau đó, bộ phận đặt hàng nước ngoài kiểm tra các site nhập khẩu có thể cung cấp những mặt hàng đó, xem thông tin tồn kho, chọn phương thức vận chuyển phù hợp, rồi gửi đơn đặt hàng đến các site được chọn. Khi hàng về, bộ phận kho ghi nhận số lượng thực nhận và so sánh với số lượng đã đặt.

Mục tiêu của project là giúp quy trình trên dễ quản lý hơn, nhất quán hơn và giảm sai sót bằng một ứng dụng desktop chạy cục bộ với cơ sở dữ liệu SQLite embedded.

## 2. Chức năng chính

- Quản lý mặt hàng: Tạo, xem và quản lý thông tin mặt hàng nhập khẩu như mã, tên, đơn vị tính và mô tả.
- Quản lý site nhập khẩu: Quản lý các overseas import site, gồm mã site, tên site, số ngày giao bằng tàu, số ngày giao bằng máy bay và thông tin bổ sung.
- Quản lý danh mục site: Xác định site nào có kinh doanh hoặc cung cấp mặt hàng nào.
- Quản lý tồn kho: Quản lý số lượng tồn kho của từng mặt hàng tại từng site nhập khẩu.
- Tạo yêu cầu nhập hàng: Cho phép bộ phận kinh doanh tạo yêu cầu nhập với mặt hàng, số lượng, đơn vị và ngày giao mong muốn.
- Thuật toán phân bổ: Phân bổ mặt hàng cần nhập cho các site phù hợp dựa trên tồn kho, hạn giao hàng và quy tắc ưu tiên vận chuyển.
- Tạo đơn đặt site: Sinh đơn đặt hàng gửi đến các site từ kết quả phân bổ thành công.
- Nhập kho: Ghi nhận số lượng thực nhận và so sánh với số lượng đã đặt để xác định đủ hàng, thiếu hàng hoặc dư hàng.

## 3. Công nghệ sử dụng

- Java 21
- JavaFX 21
- Maven
- FXML
- JavaFX CSS
- SQLite embedded database
- JDBC
- DAO pattern
- MVC architecture
- Factory Method Pattern
- JUnit 5

## 4. Kiến trúc

Project áp dụng kiến trúc MVC, tách rõ giao diện người dùng, xử lý nghiệp vụ và truy cập cơ sở dữ liệu.

### Model

Lớp Model chứa các domain object dạng POJO đại diện cho dữ liệu nghiệp vụ. Các class này không phụ thuộc JavaFX.

Ví dụ:

- `Merchandise`
- `ImportSite`
- `SiteMerchandise`
- `Inventory`
- `ImportRequest`
- `ImportRequestItem`
- `AllocationResult`
- `SiteOrder`
- `SiteOrderItem`
- `ReceivedGoods`

### View

Lớp View gồm các file FXML và JavaFX CSS. FXML định nghĩa bố cục màn hình, còn CSS định nghĩa giao diện hiển thị của ứng dụng desktop.

Ví dụ:

- `MainLayout.fxml`
- `DashboardView.fxml`
- `MerchandiseView.fxml`
- `ImportSiteView.fxml`
- `InventoryView.fxml`
- `ImportRequestView.fxml`
- `AllocationResultView.fxml`
- `SiteOrderView.fxml`
- `WarehouseReceivingView.fxml`
- `app.css`

### Controller

Lớp Controller gồm các JavaFX controller. Controller nhận sự kiện từ giao diện, đọc dữ liệu người dùng nhập, cập nhật UI và gọi Service. Controller không chứa business logic và không truy vấn database trực tiếp.

Ví dụ:

- `MerchandiseController`
- `ImportSiteController`
- `InventoryController`
- `ImportRequestController`
- `AllocationController`
- `SiteOrderController`
- `WarehouseController`

### Service

Lớp Service chứa business logic và các use case của ứng dụng. Service chịu trách nhiệm validate dữ liệu, phối hợp các DAO, thực thi quy tắc nghiệp vụ và cập nhật trạng thái workflow.

Ví dụ:

- `ImportRequestService`
- `AllocationService`
- `SiteOrderService`
- `WarehouseService`
- `InventoryService`
- `SiteCatalogService`

### DAO

Lớp DAO chứa logic truy cập cơ sở dữ liệu. DAO interface định nghĩa các thao tác dữ liệu, còn các implementation SQLite sử dụng JDBC để thực thi SQL.

Ví dụ:

- `MerchandiseDAO` / `SQLiteMerchandiseDAO`
- `ImportSiteDAO` / `SQLiteImportSiteDAO`
- `InventoryDAO` / `SQLiteInventoryDAO`
- `ImportRequestDAO` / `SQLiteImportRequestDAO`
- `AllocationResultDAO` / `SQLiteAllocationResultDAO`
- `SiteOrderDAO` / `SQLiteSiteOrderDAO`

## 5. Design Pattern

### Factory Method với DAOFactory / SQLiteDAOFactory

`DAOFactory` định nghĩa các factory method để tạo DAO object. `SQLiteDAOFactory` cung cấp các DAO implementation cụ thể cho SQLite.

Cách thiết kế này giúp service layer phụ thuộc vào abstraction của DAO thay vì phụ thuộc trực tiếp vào các class JDBC cụ thể. Nếu sau này thay đổi công nghệ database, service layer có thể ít bị ảnh hưởng.

### Factory Method với DeliveryOptionFactory

`DeliveryOptionFactory` định nghĩa factory method để tạo delivery option. Các factory cụ thể tạo ra từng chiến lược vận chuyển:

- `ShipDeliveryOptionFactory` tạo `ShipDeliveryOption`
- `AirDeliveryOptionFactory` tạo `AirDeliveryOption`

Cách thiết kế này giúp việc tạo delivery option linh hoạt hơn và tách allocation logic khỏi việc khởi tạo object vận chuyển cụ thể.

### Factory Method với ViewFactory

`ViewFactory` tập trung hóa việc tạo và load JavaFX view. Class này ánh xạ view type với file FXML tương ứng và load đúng màn hình khi người dùng điều hướng trong ứng dụng.

Nhờ đó, logic load FXML được quản lý nhất quán và không bị rải rác trong nhiều controller.

## 6. GRASP Principles

| Principle | Applied class | Explanation |
| --- | --- | --- |
| Information Expert | `AllocationService` | Class này có đủ thông tin về request item, site catalog, inventory, delivery option và allocation result, nên phù hợp để chịu trách nhiệm áp dụng quy tắc phân bổ. |
| Creator | `SQLiteDAOFactory` | Class này tạo các DAO cụ thể vì nó biết ứng dụng đang sử dụng database implementation nào. |
| Controller | `ImportRequestController`, `AllocationController`, `WarehouseController` | JavaFX controller nhận event từ UI và chuyển tiếp hành động của người dùng đến service. |
| Low Coupling | Service phụ thuộc DAO interface | Các service làm việc với DAO abstraction thay vì phụ thuộc trực tiếp vào SQLite DAO concrete class. |
| High Cohesion | `ImportRequestService`, `InventoryService`, `WarehouseService` | Mỗi service tập trung vào một nhóm nghiệp vụ cụ thể và tránh trộn nhiều trách nhiệm không liên quan. |
| Polymorphism | `DeliveryOption`, `ShipDeliveryOption`, `AirDeliveryOption` | Hành vi vận chuyển được xử lý qua abstraction chung, cho phép chọn ship hoặc air delivery mà không cần hard-code điều kiện ở nhiều nơi. |
| Pure Fabrication | `ApplicationContext`, `ViewFactory` | Các class này không phải domain entity, nhưng hỗ trợ tạo object, wiring dependency và load view để giữ hệ thống gọn gàng. |
| Protected Variations | `DAOFactory`, DAO interfaces, `DeliveryOptionFactory` | Các abstraction này bảo vệ ứng dụng trước thay đổi về database implementation, cách tạo DAO và cách tạo delivery option. |

## 7. Thuật toán phân bổ

Thuật toán phân bổ xử lý từng mặt hàng trong yêu cầu nhập một cách độc lập.

Với mỗi mặt hàng:

1. Tìm các site nhập khẩu có kinh doanh mặt hàng được yêu cầu.
2. Bỏ qua các site không có tồn kho.
3. Tính số ngày có thể giao dựa trên ngày yêu cầu và ngày giao mong muốn.
4. Chọn phương thức vận chuyển cho từng site:
   - Nếu giao bằng tàu kịp ngày giao mong muốn, chọn giao bằng tàu.
   - Nếu giao bằng tàu không kịp nhưng giao bằng máy bay kịp, chọn giao bằng máy bay.
   - Nếu cả tàu và máy bay đều không kịp, loại site đó.
5. Sắp xếp các site hợp lệ theo thứ tự ưu tiên:
   - Ưu tiên giao bằng tàu hơn giao bằng máy bay.
   - Ưu tiên site có tồn kho lớn hơn.
6. Phân bổ theo chiến lược greedy:
   - Lấy tối đa số lượng có thể từ site đang xét.
   - Chỉ chuyển sang site tiếp theo nếu số lượng còn thiếu vẫn lớn hơn 0.
   - Cách này giúp giảm số lượng site được chọn.
7. Nếu một site không đủ hàng, hệ thống có thể lấy từ nhiều site.
8. Nếu tổng tồn kho từ tất cả site hợp lệ vẫn không đủ, hệ thống tạo kết quả phân bổ thất bại kèm thông báo lỗi.

Quy tắc quan trọng: tồn kho không bị trừ khi chạy phân bổ. Tồn kho chỉ bị trừ sau khi xác nhận đơn đặt hàng.

## 8. Ghi chú cho người build/release

Docker chỉ dùng để kiểm tra build/test trong môi trường JDK 21:

```bash
docker build -t import-order-system-build .
```

Hiện tại project không còn script tạo package/installer trong thư mục `scripts/`. Cách chạy khuyến nghị cho developer là dùng script bootstrap ở cuối README.

## 9. Demo flow

1. Mở ứng dụng.
2. Kiểm tra seed data cho mặt hàng, site nhập khẩu, danh mục site và tồn kho.
3. Tạo yêu cầu nhập hàng mới từ màn hình Yêu cầu nhập hàng.
4. Thêm các mặt hàng cần nhập với số lượng, đơn vị và ngày giao mong muốn.
5. Gửi yêu cầu nhập hàng.
6. Chạy phân bổ cho yêu cầu đã gửi.
7. Xem kết quả phân bổ, gồm site được chọn, số lượng phân bổ, phương thức giao và trạng thái phân bổ.
8. Xác nhận đơn đặt để sinh site order từ các kết quả phân bổ thành công.
9. Xem các site order đã được tạo.
10. Mở màn hình Nhập kho.
11. Ghi nhận số lượng thực nhận.
12. Kiểm tra hàng nhận là đủ hàng, thiếu hàng hay dư hàng so với số lượng đã đặt.

## 10. Giả định đã biết

- `requestDate` được dùng làm ngày bắt đầu để tính số ngày vận chuyển còn lại.
- Tồn kho chỉ bị trừ sau khi Confirm Orders, không bị trừ ngay sau khi Run Allocation.
- Ứng dụng sử dụng SQLite local database vì được thiết kế là desktop application không có server backend.
- Mỗi mặt hàng trong một yêu cầu nhập được phân bổ độc lập.
- Site order được sinh từ các kết quả phân bổ thành công.
- Chức năng nhập kho so sánh số lượng thực nhận với số lượng đã đặt cho từng dòng site order item.

## 11. Cách chạy nhanh nhất sau khi clone GitHub

Người chạy project chỉ cần có:

- Git
- Internet trong lần chạy đầu tiên

Không cần tự cài JDK 21 hoặc Maven. Project đã có script tự tải đúng JDK 21 và Maven vào thư mục local `.tools/`.

### Bước 1: Clone project

```bash
git clone <repository-url>
cd import-order-system
```

Ví dụ:

```bash
git clone https://github.com/<username>/<repository-name>.git
cd import-order-system
```

Nếu đã clone trước đó và chỉ muốn cập nhật code mới nhất:

```bash
git pull
```

### Bước 2: Chạy project

Trên Windows, mở PowerShell trong thư mục project:

```powershell
.\scripts\run-windows.ps1
```

Nếu PowerShell chặn chạy script, dùng lệnh sau trong session hiện tại:

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\scripts\run-windows.ps1
```

Trên macOS, mở Terminal trong thư mục project:

```bash
chmod +x scripts/run-macos.sh
./scripts/run-macos.sh
```

Lần đầu script sẽ tải JDK 21 và Maven nên có thể mất vài phút. Các lần sau sẽ chạy nhanh hơn vì dùng lại thư mục `.tools/`.

### Bước 3: Mở ứng dụng

Sau khi script chạy xong, JavaFX app sẽ tự mở. Database SQLite được tạo tự động tại:

```text
~/.import-order-system/import_order.db
```

### Chạy nhanh không chạy test

Nếu chỉ muốn mở app nhanh và bỏ qua test:

Windows:

```powershell
.\scripts\run-windows.ps1 -SkipTests
```

macOS:

```bash
SKIP_TESTS=true ./scripts/run-macos.sh
```
